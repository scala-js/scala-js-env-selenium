package org.scalajs.jsenv.selenium

import org.openqa.selenium.chrome.{ChromeDriverService, ChromeOptions}
import org.openqa.selenium.remote._

import scala.language.implicitConversions

object Chrome {
  def apply(): Chrome = new Chrome(new ChromeOptions)

  @deprecated("Use Chrome() instead.", "0.1.3")
  implicit def useAsConfig(self: Chrome.type): Chrome = apply()
}

class Chrome private (chromeOptions: ChromeOptions) extends SeleniumBrowser {

  def name: String = "Chrome"

  def newDriver: BrowserDriver = new ChromeDriver
  def withChromeOptions(chromeOptions: ChromeOptions): Chrome = new Chrome(chromeOptions)

  private class ChromeDriver extends BrowserDriver {
    protected def newDriver(): RemoteWebDriver = {
      val service = {
        /* Activate the silent ChromeDriverService silent mode,
         * see ChromeDriverService.createDefaultService
         */
        new ChromeDriverService.Builder().withSilent(true).usingAnyFreePort.build
      }
      new org.openqa.selenium.chrome.ChromeDriver(service, chromeOptions)
    }
  }
}
