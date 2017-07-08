package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io.{MemVirtualJSFile, VirtualJSFile}
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.logging.Logger
import org.scalajs.jsenv.{JSConsole, VirtualFileMaterializer}

abstract class AbstractSeleniumJSRunner(browserProvider: SeleniumBrowser,
    libs: Seq[ResolvedJSDependency], code: VirtualJSFile, materializer: FileMaterializer) {


  protected val browser = browserProvider.newDriver

  private[this] var _logger: Logger = _
  private[this] var _console: JSConsole = _

  protected def logger: Logger = _logger
  protected def console: JSConsole = _console

  protected def setupLoggerAndConsole(logger: Logger, console: JSConsole): Unit = {
    require(_logger == null && _console == null)
    _logger = logger
    _console = console
  }

  protected def ignoreKeepAlive: Boolean = {
    val name = code.name
    name == "frameworkDetector.js" ||
    name == "testFrameworkInfo.js" ||
    name == "testMaster.js"
  }

  protected def initFiles(): Seq[VirtualJSFile] =
    browserProvider.initFiles() ++ runtimeEnv()

  protected def runAllScripts(): Unit = {
    val inits = initFiles()

    val jsFiles = {
      inits.map(materializer.materialize(_).toString) ++
      libs.map(dep => materializer.materialize(dep.lib).toString) :+
      code.path
    }
    val page = htmlPage(jsFiles)

    materializer.materialize(code)
    val pageURL = materializer.materialize(page)

    browser.getWebDriver.get(pageURL.toString)
    browser.processConsoleLogs(console)
  }

  /** File(s) to define `__ScalaJSEnv`. Defines `exitFunction`. */
  protected def runtimeEnv(): Seq[VirtualJSFile] = Seq(
    new MemVirtualJSFile("scalaJSEnvInfo.js").withContent(
      "var __ScalaJSEnv = __ScalaJSEnv || {};\n" +
      "__ScalaJSEnv.existFunction = function(status) { window.close(); };"
    )
  )

  protected def htmlPage(jsFilesPaths: Seq[String]): VirtualJSFile = {
    val scriptTags = jsFilesPaths.map(path => s"<script src='$path'></script>")
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
}
