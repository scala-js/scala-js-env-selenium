package org.scalajs.jsenv.selenium

import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.remote._
import org.openqa.selenium.logging._

import scala.collection.JavaConversions._

object Chrome extends SeleniumBrowser {
  def name: String = "Chrome"

  def newDriver: BrowserDriver = new ChromeDriver

  private class ChromeDriver extends BrowserDriver {
    protected def newDriver(): RemoteWebDriver = {
      val caps = DesiredCapabilities.chrome()
      val logPrefs = new LoggingPreferences()
      logPrefs.enable(LogType.BROWSER, java.util.logging.Level.ALL)
      caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs)

      val service = {
        // Activate the silent ChromeDriverService silent mode,
        // see ChromeDriverService.createDefaultService
        new ChromeDriverService.Builder().withSilent(true).usingAnyFreePort.build
      }

      new org.openqa.selenium.chrome.ChromeDriver(service, caps)
    }

    protected def newConsoleLogsIterator(): Iterator[String] = {
      getWebDriver.manage().logs().get(LogType.BROWSER).iterator.map { entry =>
        // The message contains a prefix identifying the source ("\S+") of the
        // log followed by the line and column numbers ("\d+:\d+").
        entry.getMessage.replaceFirst("""\S+ \d+:\d+ """, "")
      }
    }
  }
}
