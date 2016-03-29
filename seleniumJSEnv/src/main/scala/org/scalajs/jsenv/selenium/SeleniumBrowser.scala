package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io._

trait SeleniumBrowser {
  def newDriver: BrowserDriver
  def name: String
  def initFiles(): Seq[VirtualJSFile] =
    setupConsoleCapture()

  /** The default selenium console output is different in each version.
   *  We use a custom structure to log Scala.js console outputs.
   */
  protected def setupConsoleCapture(): Seq[VirtualJSFile] = Seq(
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
}
