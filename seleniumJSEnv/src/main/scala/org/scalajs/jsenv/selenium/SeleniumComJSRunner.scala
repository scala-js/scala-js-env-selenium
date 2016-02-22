package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io.{MemVirtualJSFile, VirtualJSFile}
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.jsenv.ComJSRunner

import java.util.concurrent.TimeoutException

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

class SeleniumComJSRunner(browserProvider: SeleniumBrowser,
    libs: Seq[ResolvedJSDependency], code: VirtualJSFile, keepAlive: Boolean)
    extends SeleniumAsyncJSRunner(browserProvider, libs, code, keepAlive)
    with ComJSRunner {

  protected def envName: String =
    "SeleniumComJSRunner on " + browserProvider.name

  def send(msg: String): Unit = {
    awaitForBrowser()
    browser.getWebDriver.executeScript(
        "this.scalajsSeleniumComJSRunnerChannel.recvMessage(arguments[0])", msg)
    browser.processConsoleLogs(console)
  }

  def receive(timeout: Duration): String = {
    awaitForBrowser(timeout)
    @tailrec def loop(): String = {
      val script = "return this.scalajsSeleniumComJSRunnerChannel.popOutMsg();"
      browser.getWebDriver.executeScript(script) match {
        case null =>
          loop()

        case msg: String =>
          browser.processConsoleLogs(console)
          msg

        case obj =>
          // Here we only try to get the console because it uses the same
          // communication channel that is potentially corrupted.
          Try(browser.processConsoleLogs(console))
          BrowserDriver.illFormattedScriptResult(obj)
      }
    }
    loop()
  }

  def close(): Unit = {
    browser.processConsoleLogs(console)
    if (!keepAlive)
      browser.close()
  }

  override protected def initFiles(): Seq[VirtualJSFile] =
    super.initFiles() :+ comSetupFile()

  protected def comSetupFile(): VirtualJSFile = {
    val code = {
      s"""
         |(function() {
         |  // Buffers for outgoing messages
         |  var sendMsgBufIn = []; // push to this one
         |  var sendMsgBufOut = []; // pop from this one
         |
         |  var onReceive = null;
         |
         |  this.scalajsCom = {
         |    init: function(recvCB) {
         |      onReceive = recvCB;
         |    },
         |    send: function(msg) {
         |      sendMsgBufIn.push(msg);
         |    },
         |    close: function() {
         |      // Nothing to close, channel is managed by Selenium.
         |    }
         |  };
         |
         |  this.scalajsSeleniumComJSRunnerChannel = {
         |    popOutMsg: function() {
         |      if (sendMsgBufOut.length == 0) {
         |        sendMsgBufOut = sendMsgBufIn.reverse();
         |        sendMsgBufIn = [];
         |      }
         |      return sendMsgBufOut.pop();
         |    },
         |    recvMessage: function(msg) {
         |      onReceive(msg);
         |    }
         |  };
         |}).call(this);
      """.stripMargin
    }
    new MemVirtualJSFile("comSetup.js").withContent(code)
  }

  protected def awaitForBrowser(timeout: Duration = Duration.Inf): Unit = {
    if (!Await.ready(future, timeout).isCompleted)
      throw new TimeoutException()
  }
}
