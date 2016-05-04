package org.scalajs.jsenv.selenium

import java.{util => ju}
import java.util.concurrent.TimeUnit
import java.util.logging.Level

import org.openqa.selenium.remote._

import org.scalajs.jsenv.JSConsole

import scala.annotation.tailrec
import scala.collection.JavaConversions._

abstract class BrowserDriver {
  import BrowserDriver._

  private var webDriver: RemoteWebDriver = _

  final def getWebDriver: RemoteWebDriver = synchronized {
    if (webDriver == null)
      throw new BrowserDriver.BrowserNotOpenException
    webDriver
  }

  /** Starts a new instance of the browser. */
  final def start(): Unit = synchronized {
    assert(!isOpened, "start() may only start one instance at a time.")
    webDriver = newDriver()
    webDriver.manage().timeouts().setScriptTimeout(-1, TimeUnit.SECONDS)
  }

  /** Closes the instance of the browser. */
  final def close(): Unit = synchronized {
    if (isOpened) {
      webDriver.close()
      webDriver = null
    }
  }

  final def isOpened: Boolean = synchronized {
    webDriver != null
  }

  /** Tries to get the console logs from the browser and prints them on the
   *  JSConsole. This method never throws BrowserNotOpenException.
   */
  final def processConsoleLogs(console: JSConsole): Unit = {
    try {
      @tailrec def processNextLogBatch(): Unit = {
        getWebDriver.executeAsyncScript(popCapturedConsoleScript) match {
          case logs: ju.List[_] =>
            logs.foreach(console.log)
            if (logs.size() != 0)
              processNextLogBatch()

          case msg => BrowserDriver.illFormattedScriptResult(msg)
        }
      }
      processNextLogBatch()
    } catch {
      case _: BrowserDriver.BrowserNotOpenException => // Do nothing
    }
  }

  def browserErrors(): List[String] = {
    val logs = getWebDriver.manage().logs().get("browser").iterator()
    logs.collect {
      case log if log.getLevel == Level.SEVERE => log.getMessage
    }.toList
  }

  protected def newDriver(): RemoteWebDriver
}

object BrowserDriver {
  class BrowserNotOpenException extends Exception

  private def popCapturedConsoleScript = {
    "var callback = arguments[arguments.length - 1];" +
    "callback(this.scalajsPopCapturedConsoleLogs());"
  }

  private[selenium] def illFormattedScriptResult(obj: Any): Nothing = {
    throw new IllegalStateException(
        s"Receive ill formed message of type ${obj.getClass} with value: $obj")
  }
}
