package org.scalajs.jsenv.selenium

import org.openqa.selenium.remote.RemoteWebDriver

object Firefox extends SeleniumBrowser {
  def name: String = "Firefox"

  def newDriver: BrowserDriver = new FirefoxDriver

  private class FirefoxDriver extends BrowserDriver {
    protected def newDriver(): RemoteWebDriver =
      new org.openqa.selenium.firefox.FirefoxDriver()
  }
}
