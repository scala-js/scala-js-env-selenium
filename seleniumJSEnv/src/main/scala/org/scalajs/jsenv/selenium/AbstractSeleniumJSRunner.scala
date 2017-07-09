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

  protected def setupLoggerAndConsole(logger: Logger, console: JSConsole): Unit = {
    require(_logger == null && _console == null)
    _logger = logger
    _console = console
  }

  protected def start(): Unit = synchronized {
    assert(_driver == null, "start() may only start one instance at a time.")
    _driver = factory()
  }

  protected def close(): Unit = synchronized {
    if (_driver != null) {
      _driver.close()
      _driver = null
    }
  }

  protected def ignoreKeepAlive: Boolean = {
    val name = code.name
    name == "frameworkDetector.js" ||
    name == "testFrameworkInfo.js" ||
    name == "testMaster.js"
  }

  protected def initFiles(): Seq[VirtualJSFile] =
    setupConsoleCapture() ++ runtimeEnv()

  protected def runAllScripts(): Unit = {
    val inits = initFiles()

    val jsFiles = {
      inits.map(config.materializer.materialize(_).toString) ++
      libs.map(dep => config.materializer.materialize(dep.lib).toString) :+
      code.path
    }
    val page = htmlPage(jsFiles)

    config.materializer.materialize(code)
    val pageURL = config.materializer.materialize(page)

    driver.get(pageURL.toString)
    processConsoleLogs(console)
  }

  /** Tries to get the console logs from the browser and prints them on the
   *  JSConsole.
   */
  final protected def processConsoleLogs(console: JSConsole): Unit = {
    if (_driver != null) {
      @tailrec def processNextLogBatch(): Unit = {
        val code = "return this.scalajsPopCapturedConsoleLogs()"
        driver.executeScript(code) match {
          case logs: java.util.List[_] =>
            logs.asScala.foreach(console.log)
            if (logs.size() != 0)
              processNextLogBatch()

          case msg => illFormattedScriptResult(msg)
        }
      }
      processNextLogBatch()
    }
  }

  protected def browserErrors(): List[String] = {
    import java.util.logging.Level

    val logs = driver.manage().logs().get("browser").iterator()
    logs.asScala.collect {
      case log if log.getLevel == Level.SEVERE => log.getMessage
    }.toList
  }

  private def setupConsoleCapture(): Seq[VirtualJSFile] = Seq(
    new MemVirtualJSFile("setupConsoleCapture.js").withContent(
      s"""
        |(function () {
        |  var console_captured_logs = [];
        |  var currentLogIndex = 0;
        |  var oldLog = console.log;
        |  console.log = function (msg) {
        |    console_captured_logs.push(msg);
        |    oldLog.apply(console, arguments);
        |  };
        |  var oldErr = console.error;
        |  console.error = function (msg) {
        |    console_captured_logs.push(msg);
        |    oldErr.apply(console, arguments);
        |  };
        |  this.scalajsPopCapturedConsoleLogs = function () {
        |    if (console_captured_logs.length == 0) {
        |      return console_captured_logs;
        |    } else {
        |      var log = [];
        |      while (currentLogIndex < console_captured_logs.length &&
        |          log.length < 1024) {
        |        log.push(String(console_captured_logs[currentLogIndex]));
        |        currentLogIndex++;
        |      }
        |      if (console_captured_logs.length == currentLogIndex) {
        |        console_captured_logs = [];
        |        currentLogIndex = 0;
        |      }
        |      return log;
        |    }
        |  };
        |})();
      """.stripMargin
    )
  )

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

  protected def illFormattedScriptResult(obj: Any): Nothing = {
    throw new IllegalStateException(
        s"Receive ill formed message of type ${obj.getClass} with value: $obj")
  }
}

private[selenium] object AbstractSeleniumJSRunner {
  type DriverFactory = () => WebDriver with JavascriptExecutor
}
