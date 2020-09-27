package org.scalajs.jsenv.selenium

import org.openqa.selenium._

import org.scalajs.jsenv._

import scala.annotation.tailrec
import scala.concurrent._
import scala.util.control.NonFatal

import java.util.concurrent.{ConcurrentLinkedQueue, Executors}
import java.util.function.Consumer
import java.nio.file.Path
import java.net.URL

private sealed class SeleniumRun(
    driver: WebDriver with JavascriptExecutor,
    config: SeleniumJSEnv.Config,
    streams: OutputStreams.Streams,
    materializer: FileMaterializer) extends JSRun {

  @volatile
  private[this] var wantClose = false

  protected val intf = "this.scalajsSeleniumInternalInterface"

  private[this] implicit val ec =
    ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

  private val handler = Future {
    while (!isInterfaceUp() && !wantClose) {
      Thread.sleep(100)
    }

    while (!wantClose) {
      sendAll()
      fetchAndProcess()
      Thread.sleep(100)
    }
  }

  val future: Future[Unit] = handler.andThen { case _ =>
    SeleniumRun.maybeCleanupDriver(driver, config)
    streams.close()
    materializer.close()
  }

  def close(): Unit = wantClose = true

  private final def fetchAndProcess(): Unit = {
    import SeleniumRun.consumer

    val data = driver
      .executeScript(s"return $intf.fetch();")
      .asInstanceOf[java.util.Map[String, java.util.List[String]]]

    data.get("consoleLog").forEach(consumer(streams.out.println _))
    data.get("consoleError").forEach(consumer(streams.err.println _))
    data.get("msgs").forEach(consumer(receivedMessage _))

    val errs = data.get("errors")
    if (!errs.isEmpty()) {
      // Convoluted way of writing errs.toList without JavaConverters.
      val errList = errs.toArray(Array[String]()).toList
      throw new SeleniumRun.WindowOnErrorException(errList)
    }
  }

  private final def isInterfaceUp() =
    driver.executeScript(s"return !!$intf;").asInstanceOf[Boolean]

  // Hooks for SeleniumComRun.

  protected def sendAll(): Unit = ()

  protected def receivedMessage(msg: String): Unit =
    throw new AssertionError(s"received message in non-com run: $msg")
}

private final class SeleniumComRun(
    driver: WebDriver with JavascriptExecutor,
    config: SeleniumJSEnv.Config,
    streams: OutputStreams.Streams,
    materializer: FileMaterializer,
    onMessage: String => Unit)
    extends SeleniumRun(driver, config, streams, materializer) with JSComRun {
  private[this] val sendQueue = new ConcurrentLinkedQueue[String]

  def send(msg: String): Unit = sendQueue.offer(msg)

  override protected def receivedMessage(msg: String) = onMessage(msg)

  @tailrec
  override protected final def sendAll(): Unit = {
    val msg = sendQueue.poll()
    if (msg != null) {
      driver.executeScript(s"$intf.send(arguments[0]);", msg)
      sendAll()
    }
  }
}

private[selenium] object SeleniumRun {
  import SeleniumJSEnv.Config
  import OutputStreams.Streams

  type JSDriver = WebDriver with JavascriptExecutor

  private lazy val validator = {
    RunConfig.Validator()
      .supportsInheritIO()
      .supportsOnOutputStream()
  }

  def start(newDriver: () => JSDriver, input: Seq[Input], config: Config,
      runConfig: RunConfig): JSRun = {
    startInternal(newDriver, input, config, runConfig, enableCom = false)(
        new SeleniumRun(_, _, _, _), JSRun.failed _)
  }

  def startWithCom(newDriver: () => JSDriver, input: Seq[Input], config: Config,
      runConfig: RunConfig, onMessage: String => Unit): JSComRun = {
    startInternal(newDriver, input, config, runConfig, enableCom = true)(
        new SeleniumComRun(_, _, _, _, onMessage), JSComRun.failed _)
  }

  private type Ctor[T] = (JSDriver, Config, Streams, FileMaterializer) => T

  private def startInternal[T](newDriver: () => JSDriver, input: Seq[Input],
      config: Config, runConfig: RunConfig, enableCom: Boolean)(
      newRun: Ctor[T], failed: Throwable => T): T = {
    validator.validate(runConfig)

    try {
      withCleanup(FileMaterializer(config.materialization))(_.close()) { m =>
        val setupJsScript = Input.Script(JSSetup.setupFile(enableCom))
        val fullInput = setupJsScript +: input
        val page = m.materialize("scalajsRun.html", htmlPage(fullInput, m))
        withCleanup(newDriver())(maybeCleanupDriver(_, config)) { driver =>
          driver.navigate().to(page)

          withCleanup(OutputStreams.prepare(runConfig))(_.close()) { streams =>
            newRun(driver, config, streams, m)
          }
        }
      }
    } catch {
      case NonFatal(t) =>
        failed(t)
    }
  }

  private def withCleanup[V, R](mk: => V)(cleanup: V => Unit)(body: V => R): R = {
    val v = mk
    try {
      body(v)
    } catch {
      case t: Throwable =>
        cleanup(v)
        throw t
    }
  }

  private def maybeCleanupDriver(d: WebDriver, config: SeleniumJSEnv.Config) =
    if (!config.keepAlive) d.close()

  private def htmlPage(fullInput: Seq[Input], materializer: FileMaterializer): String = {
    val tags = fullInput.map {
      case Input.Script(path)   => makeTag(path, "text/javascript", materializer)
      case Input.ESModule(path) => makeTag(path, "module", materializer)
      case _                    => throw new UnsupportedInputException(fullInput)
    }

    s"""<html>
       |  <meta charset="UTF-8">
       |  <body>
       |    ${tags.mkString("\n    ")}
       |  </body>
       |</html>
    """.stripMargin
  }

  private def makeTag(path: Path, tpe: String, materializer: FileMaterializer): String = {
    val url = materializer.materialize(path)
    s"<script defer type='$tpe' src='$url'></script>"
  }

  private class WindowOnErrorException(errs: List[String]) extends Exception(s"JS error: $errs")

  private def consumer[A](f: A => Unit): Consumer[A] = new Consumer[A] {
    override def accept(v: A): Unit = f(v)
  }
}
