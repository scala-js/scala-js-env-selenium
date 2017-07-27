package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io.VirtualJSFile
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.logging.Logger
import org.scalajs.jsenv.{JSConsole, AsyncJSRunner}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.Try

private[selenium] class SeleniumAsyncJSRunner(
    factory: AbstractSeleniumJSRunner.DriverFactory,
    libs: Seq[ResolvedJSDependency], code: VirtualJSFile,
    config: SeleniumJSEnv.Config)
    extends AbstractSeleniumJSRunner(factory, libs, code, config)
    with AsyncJSRunner {

  private[this] val promise = Promise[Unit]()

  protected def initFuture: Future[Unit] = promise.future

  def future: Future[Unit] = initFuture

  def start(logger: Logger, console: JSConsole): Future[Unit] = synchronized {
    startInternal(logger, console)
    (new SeleniumAsyncJSRunnerThread).start()
    future
  }

  private class SeleniumAsyncJSRunnerThread extends Thread {
    override def run(): Unit = {
      // This thread should not be interrupted, so it is safe to use Trys
      val runnerInit = Try(runAllScripts())

      if (runnerInit.isFailure)
        processConsoleLogs(console)

      promise.complete(runnerInit)
    }
  }
}
