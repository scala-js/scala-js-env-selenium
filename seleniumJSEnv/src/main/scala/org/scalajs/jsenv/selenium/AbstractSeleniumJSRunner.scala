package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io.{MemVirtualJSFile, VirtualJSFile}
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.logging.Logger
import org.scalajs.jsenv.{JSConsole, VirtualFileMaterializer}

import org.openqa.selenium.{WebDriver, JavascriptExecutor}

import scala.annotation.tailrec
import scala.collection.JavaConverters._

private[selenium] abstract class AbstractSeleniumJSRunner(
    factory: AbstractSeleniumJSRunner.DriverFactory,
    libs: Seq[ResolvedJSDependency], code: VirtualJSFile,
    config: SeleniumJSEnv.Config) {

  private[this] var _logger: Logger = _
  private[this] var _console: JSConsole = _
  private[this] var _driver: WebDriver with JavascriptExecutor = _

  protected def logger: Logger = _logger
  protected def console: JSConsole = _console
  protected def driver: WebDriver with JavascriptExecutor = _driver

  protected def startInternal(logger: Logger, console: JSConsole): Unit = synchronized {
    require(_driver == null, "start() may only start one instance at a time.")
    require(_logger == null && _console == null)
    _logger = logger
    _console = console
    _driver = factory()
  }

  def stop(): Unit = synchronized {
    if ((!config.keepAlive || ignoreKeepAlive) && _driver != null) {
      _driver.close()
      _driver = null
    }
  }

  private def ignoreKeepAlive: Boolean = {
    val name = code.name
    name == "frameworkDetector.js" ||
    name == "testFrameworkInfo.js" ||
    name == "testMaster.js"
  }

  protected def initFiles(): Seq[VirtualJSFile] =
    setupCapture() ++ runtimeEnv()

  protected def runAllScripts(): Unit = {
    val jsFiles = initFiles() ++ libs.map(_.lib) :+ code
    val page = htmlPage(jsFiles.map(config.materializer.materialize _))
    val pageURL = config.materializer.materialize(page)

    driver.get(pageURL.toString)
    processConsoleLogs(console)
  }

  private def callPop(cmd: String): Seq[_] = {
    if (_driver != null) {
      /* We need to check the existence of the command since we race with the
       * browser setup.
       */
      val code = s"return this.$cmd && this.$cmd();"
      driver.executeScript(code) match {
        case null                    => Seq()
        case logs: java.util.List[_] => logs.asScala
        case msg                     => illFormattedScriptResult(msg)
      }
    } else {
      Seq()
    }
  }

  /** Tries to get the console logs from the browser and prints them on the
   *  JSConsole.
   */
  final protected def processConsoleLogs(console: JSConsole): Unit =
    callPop("scalajsPopCapturedConsoleLogs").foreach(console.log)

  final protected def browserErrors(): List[String] =
    callPop("scalajsPopCapturedErrors").map(_.toString).toList

  private def setupCapture(): Seq[VirtualJSFile] = Seq(
    new MemVirtualJSFile("setupConsoleCapture.js").withContent(
      s"""
        |(function () {
        |  var captured_logs = [];
        |  var captured_errors = [];
        |
        |  function captureConsole(fun) {
        |    if (!fun) return fun;
        |    return function(msg) {
        |      captured_logs.push(msg);
        |      return fun.apply(console, arguments);
        |    }
        |  }
        |
        |  console.log = captureConsole(console.log);
        |  console.error = captureConsole(console.error);
        |
        |  window.addEventListener('error', function(msg) {
        |    captured_errors.push(String(msg));
        |  });
        |
        |  this.scalajsPopCapturedConsoleLogs = function() {
        |    var logs = captured_logs;
        |    captured_logs = [];
        |    return logs;
        |  };
        |
        |  this.scalajsPopCapturedErrors = function() {
        |    var errors = captured_errors;
        |    captured_errors = [];
        |    return errors;
        |  };
        |})();
      """.stripMargin
    )
  )

  /** File(s) to define `__ScalaJSEnv`. Defines `exitFunction`. */
  private def runtimeEnv(): Seq[VirtualJSFile] = Seq(
    new MemVirtualJSFile("scalaJSEnvInfo.js").withContent(
      "var __ScalaJSEnv = __ScalaJSEnv || {};\n" +
      "__ScalaJSEnv.existFunction = function(status) { window.close(); };"
    )
  )

  private def htmlPage(scripts: Seq[java.net.URL]): VirtualJSFile = {
    val scriptTags =
      scripts.map(path => s"<script src='${path.toString}'></script>")
    val pageCode = {
      s"""<html>
         |  <meta charset="UTF-8">
         |  <body>
         |    ${scriptTags.mkString("\n    ")}
         |  </body>
         |</html>
      """.stripMargin
    }
    new MemVirtualJSFile("scalajsRun.html").withContent(pageCode)
  }

  protected def illFormattedScriptResult(obj: Any): Nothing = {
    throw new IllegalStateException(
        s"Receive ill formed message of type ${obj.getClass} with value: $obj")
  }

  protected override def finalize(): Unit = {
    stop()
    super.finalize()
  }
}

private[selenium] object AbstractSeleniumJSRunner {
  type DriverFactory = () => WebDriver with JavascriptExecutor
}
