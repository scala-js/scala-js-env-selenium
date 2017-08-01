package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io.VirtualJSFile
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.logging.Logger
import org.scalajs.jsenv.{JSConsole, AsyncJSRunner}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

private[selenium] class SeleniumAsyncJSRunner(
    factory: AbstractSeleniumJSRunner.DriverFactory,
    libs: Seq[ResolvedJSDependency], code: VirtualJSFile,
    config: SeleniumJSEnv.Config)
    extends AbstractSeleniumJSRunner(factory, libs, code, config)
    with AsyncJSRunner {

  private[this] val promise = Promise[Unit]()

  def future: Future[Unit] = promise.future

  def start(logger: Logger, console: JSConsole): Future[Unit] = synchronized {
    setupRun(console)
    Future(asyncStart())
    future
  }

  def stop(): Unit = synchronized {
    // Make sure stop is idempotent.
    if (!promise.isCompleted) {
      promise.complete(endRun())
    }
  }

  protected def asyncStart(): Unit = {
    try runAllScripts()
    finally stop()
  }
}
