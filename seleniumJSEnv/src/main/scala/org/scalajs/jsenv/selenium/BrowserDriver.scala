package org.scalajs.jsenv.selenium

import java.util.concurrent.TimeUnit

import org.openqa.selenium.remote._

import org.scalajs.jsenv.JSConsole

abstract class BrowserDriver {

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
      newConsoleLogsIterator().foreach(console.log)
    } catch {
      case _: BrowserDriver.BrowserNotOpenException => // Do nothing
    }
  }

  protected def newConsoleLogsIterator(): Iterator[String]

  protected def newDriver(): RemoteWebDriver
}

object BrowserDriver {
  class BrowserNotOpenException extends Exception

  private[selenium] def illFormattedScriptResult(obj: Any): Nothing = {
    throw new IllegalStateException(
        s"Receive ill formed message of type ${obj.getClass} with value: $obj")
  }
}
