package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io._

import org.openqa.selenium.remote._

import java.{util => ju}

import scala.collection.JavaConversions._

object Firefox extends SeleniumBrowser {
  def name: String = "Firefox"

  def newDriver: BrowserDriver = new FirefoxDriver

  override def initFiles(): Seq[VirtualJSFile] =
    hijackConsole() +: super.initFiles()

  /** The default selenium console output is broken, this is the workaround. */
  private def hijackConsole(): VirtualJSFile = {
    new MemVirtualJSFile("hijackConsole.js").withContent(
      """
        |(function () {
        |  var console_log_hijack = [];
        |  var oldLog = console.log;
        |  console.log = function (msg) {
        |    console_log_hijack.push(msg);
        |    oldLog.apply(console, arguments);
        |  };
        |  var oldErr = console.error;
        |  console.error = function (msg) {
        |    console_log_hijack.push(msg);
        |    oldErr.apply(console, arguments);
        |  };
        |  this.scalajsPopHijackedConsoleLog = function () {
        |    var log = console_log_hijack;
        |    if (log.length != 0)
        |      console_log_hijack = [];
        |    return log;
        |  };
        |})();
      """.stripMargin
    )
  }

  private class FirefoxDriver extends BrowserDriver {
    protected def newDriver(): RemoteWebDriver =
      new org.openqa.selenium.firefox.FirefoxDriver()

    protected def newConsoleLogsIterator(): Iterator[String] = {
      getWebDriver.executeScript("return scalajsPopHijackedConsoleLog();") match {
        case log: ju.ArrayList[_] => log.iterator().map(_.toString)
        case msg                    => BrowserDriver.illFormattedScriptResult(msg)
      }
    }
  }
}
