package org.scalajs.jsenv.selenium

import org.openqa.selenium.remote.service.DriverCommandExecutor
import org.openqa.selenium.remote.{DesiredCapabilities, RemoteWebDriver}
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.remote._

import scala.language.implicitConversions

object Chrome {
  def apply(): Chrome = new Chrome(DesiredCapabilities.chrome())

  @deprecated("Use Chrome() instead.", "0.1.3")
  implicit def useAsConfig(self: Chrome.type): Chrome = apply()
}

class Chrome private (capabilities: DesiredCapabilities) extends SeleniumBrowser {

  def name: String = "Chrome"

  def newDriver: BrowserDriver = new ChromeDriver
  def withChromeOptions(capabilities: DesiredCapabilities): Chrome = new Chrome(capabilities)

  private class ChromeDriver extends BrowserDriver {
    protected def newDriver(): RemoteWebDriver = {
      val service = new DriverCommandExecutor(
        /* Activate the silent ChromeDriverService silent mode,
         * see ChromeDriverService.createDefaultService
         */
        new ChromeDriverService.Builder().withSilent(true).usingAnyFreePort.build
      )
      new RemoteWebDriver(service, capabilities)
    }
  }
}
