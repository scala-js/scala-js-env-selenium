package org.scalajs.jsenv.selenium

import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.remote._

object Chrome extends SeleniumBrowser {
  def name: String = "Chrome"

  def newDriver: BrowserDriver = new ChromeDriver

  private class ChromeDriver extends BrowserDriver {
    protected def newDriver(): RemoteWebDriver = {
      val caps = DesiredCapabilities.chrome()
      val service = {
        /* Activate the silent ChromeDriverService silent mode,
         * see ChromeDriverService.createDefaultService
         */
        new ChromeDriverService.Builder().withSilent(true).usingAnyFreePort.build
      }
      new org.openqa.selenium.chrome.ChromeDriver(service, caps)
    }
  }
}
