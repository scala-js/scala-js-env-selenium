package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io.VirtualJSFile
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.logging.Logger
import org.scalajs.jsenv.{JSConsole, AsyncJSRunner}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.Try

class SeleniumAsyncJSRunner(browserProvider: SeleniumBrowser,
    libs: Seq[ResolvedJSDependency], code: VirtualJSFile, keepAlive: Boolean, materializer: FileMaterializer)
    extends AbstractSeleniumJSRunner(browserProvider, libs, code, materializer)
    with AsyncJSRunner {

  @deprecated("Use the overload with an explicit FileMaterializer.", "0.1.2")
  def this(browserProvider: SeleniumBrowser, libs: Seq[ResolvedJSDependency],
      code: VirtualJSFile, keepAlive: Boolean) = {
    this(browserProvider, libs, code, keepAlive, DefaultFileMaterializer)
  }

  private[this] var promise = Promise[Unit]()

  def future: Future[Unit] = promise.future

  def start(logger: Logger, console: JSConsole): Future[Unit] = synchronized {
    setupLoggerAndConsole(logger, console)
    promise = Promise[Unit]()
    (new SeleniumAsyncJSRunnerThread).start()
    future
  }

  override def stop(): Unit = synchronized {
    if (!keepAlive) {
      future.onComplete { _ =>
        browser.processConsoleLogs(console)
        browser.close()
      }
    }
  }

  private class SeleniumAsyncJSRunnerThread extends Thread {
    override def run(): Unit = {
      // This thread should not be interrupted, so it is safe to use Trys
      val runnerInit = Try {
        browser.start()
        runAllScripts()
      }

      if (runnerInit.isFailure)
        browser.processConsoleLogs(console)

      promise.complete(runnerInit)
    }
  }
}
