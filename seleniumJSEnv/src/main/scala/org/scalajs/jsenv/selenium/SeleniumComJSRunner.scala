package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io.{MemVirtualJSFile, VirtualJSFile}
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.jsenv.{ComJSEnv, ComJSRunner}
import java.util.concurrent.TimeoutException

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.util.Try

private[selenium] class SeleniumComJSRunner(
    factory: AbstractSeleniumJSRunner.DriverFactory,
    libs: Seq[ResolvedJSDependency], code: VirtualJSFile,
    config: SeleniumJSEnv.Config)
    extends SeleniumAsyncJSRunner(factory, libs, code, config)
    with ComJSRunner {

  private final val MESSAGE_TAG = "M"
  private final val CLOSE_TAG = "CLOSE"

  private[this] var comClosed = false
  private[this] var initCompleted = false

  override protected def asyncStart(): Unit = {
    runAllScripts()
    synchronized {
      initCompleted = true
      notifyAll()
    }
  }

  def send(msg: String): Unit = synchronized {
    while (!initCompleted)
      wait()

    if (comClosed)
      throw new ComJSEnv.ComClosedException

    val code =
      "this.scalajsSeleniumComJSRunnerChannel.recvMessage(arguments[0]);";
    driver.executeScript(code, msg);
    processConsoleLogs(console)
  }

  def receive(timeout: Duration): String = synchronized {
    val deadline = timeout match {
      case timeout: FiniteDuration => Some(timeout.fromNow)
      case _                       => None
    }

    def isOverdue = deadline.exists(_.isOverdue())

    while (!initCompleted && !isOverdue)
      deadline.fold(wait())(d => wait(d.timeLeft.toMillis))

    if (!initCompleted)
      throw new TimeoutException

    @tailrec def loop(): String = {
      if (comClosed)
        throw new ComJSEnv.ComClosedException

      if (isOverdue)
        throw new TimeoutException

      val code = "return this.scalajsSeleniumComJSRunnerChannel.popOutMsg();"
      driver.executeScript(code) match {
        case null =>
          wait(10)
          loop()

        case msg: String =>
          processConsoleLogs(console)
          if (msg.startsWith(MESSAGE_TAG)) {
            msg.substring(MESSAGE_TAG.length)
          } else if (msg == CLOSE_TAG) {
            comClosed = true
            throw new ComJSEnv.ComClosedException("Closed from browser.")
          } else {
            illFormattedScriptResult(msg)
          }

        case obj =>
          // Here we only try to get the console because it uses the same
          // communication channel that is potentially corrupted.
          Try(processConsoleLogs(console))
          illFormattedScriptResult(obj)
      }
    }
    loop()
  }

  override def stop(): Unit = synchronized {
    comClosed = true

    /* Someone (yes, me) was trying to be smart and call close from stop in
     * ComJSRunner. So this recursively calls itself if we don't select the
     * parent class explicitly.
     */
    super[SeleniumAsyncJSRunner].stop()
  }

  def close(): Unit = stop()

  override protected def initFiles(): Seq[VirtualJSFile] =
    super.initFiles() :+ comSetupFile()

  private def comSetupFile(): VirtualJSFile = {
    val code = {
      s"""
         |(function() {
         |  // Buffers for outgoing messages
         |  var sendMsgBuf = [];
         |
         |  // Buffer for incoming messages (used if onReceive not initialized)
         |  var receiveBuf = [];
         |
         |  var onReceive = null;
         |
         |  this.scalajsCom = {
         |    init: function(recvCB) {
         |      onReceive = recvCB;
         |      for (var i in receiveBuf)
         |        onReceive(receiveBuf[i]);
         |      receiveBuf = null;
         |    },
         |    send: function(msg) {
         |      sendMsgBuf.push("$MESSAGE_TAG" + msg);
         |    },
         |    close: function() {
         |      sendMsgBuf.push("$CLOSE_TAG");
         |    }
         |  };
         |
         |  this.scalajsSeleniumComJSRunnerChannel = {
         |    popOutMsg: function() { return sendMsgBuf.shift(); },
         |    recvMessage: function(msg) {
         |      if (onReceive != null) {
         |        onReceive(msg);
         |      } else {
         |        receiveBuf.push(msg);
         |      }
         |    }
         |  };
         |}).call(this);
      """.stripMargin
    }
    new MemVirtualJSFile("comSetup.js").withContent(code)
  }
}
