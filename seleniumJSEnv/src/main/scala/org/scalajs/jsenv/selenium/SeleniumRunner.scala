package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io.VirtualJSFile
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.logging.Logger
import org.scalajs.jsenv.{JSConsole, JSRunner}

private[selenium] class SeleniumRunner(
    factory: AbstractSeleniumJSRunner.DriverFactory,
    libs: Seq[ResolvedJSDependency], code: VirtualJSFile,
    config: SeleniumJSEnv.Config)
    extends AbstractSeleniumJSRunner(factory, libs, code, config) with JSRunner {

  def run(logger: Logger, console: JSConsole): Unit = {
    setupRun(logger, console)
    runAllScripts()
    endRun().get
  }
}
