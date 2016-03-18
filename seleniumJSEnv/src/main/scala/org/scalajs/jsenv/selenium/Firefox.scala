package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io._

import org.openqa.selenium.remote._

import java.{util => ju}

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

object Firefox extends SeleniumBrowser {

  private final val popHijackedConsoleScript = {
    "var callback = arguments[arguments.length - 1];" +
    "callback(scalajsPopHijackedConsoleLog());"
  }

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
        |  var currentLogIndex = 0;
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
        |    if (console_log_hijack.length == 0) {
        |      return console_log_hijack;
        |    } else {
        |      var log = [];
        |      while (currentLogIndex < console_log_hijack.length &&
        |          log.length < 1024) {
        |        log.push(console_log_hijack[currentLogIndex]);
        |        currentLogIndex++;
        |      }
        |      if (console_log_hijack.length == currentLogIndex) {
        |        console_log_hijack = [];
        |        currentLogIndex = 0;
        |      }
        |      return log;
        |    }
        |  };
        |})();
      """.stripMargin
    )
  }

  private class FirefoxDriver extends BrowserDriver {
    protected def newDriver(): RemoteWebDriver =
      new org.openqa.selenium.firefox.FirefoxDriver()

    protected def newConsoleLogsIterator(): Iterator[String] = {
      val buf = new ArrayBuffer[String]
      @tailrec def addRemainingLogsToBuffer(): Unit = {
        getWebDriver.executeAsyncScript(popHijackedConsoleScript) match {
          case logs: ju.List[_] =>
            logs.foreach(log => buf.append(log.toString))
            if (logs.size() != 0)
              addRemainingLogsToBuffer()

          case msg => BrowserDriver.illFormattedScriptResult(msg)
        }
      }
      addRemainingLogsToBuffer()
      buf.toArray.toIterator
    }
  }
}
