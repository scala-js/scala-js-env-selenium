package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io.VirtualJSFile
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.logging.Logger
import org.scalajs.jsenv.{JSConsole, JSRunner}

class SeleniumRunner(browserProvider: SeleniumBrowser,
    libs: Seq[ResolvedJSDependency], code: VirtualJSFile, keepAlive: Boolean, materializer: FileMaterializer)
    extends AbstractSeleniumJSRunner(browserProvider, libs, code, materializer) with JSRunner {

  def run(logger: Logger, console: JSConsole): Unit = {
    setupLoggerAndConsole(logger, console)
    browser.start()
    runAllScripts()
    val browserErrors = browser.browserErrors()

    if (!keepAlive || ignoreKeepAlive)
      browser.close()

    if (browserErrors.nonEmpty) {
      val msg = ("Errors caught by browser:" :: browserErrors).mkString("\n")
      throw new Exception(msg)
    }
  }
}
