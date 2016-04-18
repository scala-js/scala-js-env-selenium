package org.scalajs.jsenv.selenium

import org.openqa.selenium.remote.RemoteWebDriver

import scala.language.implicitConversions

object Firefox {
  def apply(): Firefox = new Firefox

  @deprecated("Use Firefox() instead.", "0.1.3")
  implicit def useAsConfig(self: Firefox.type): Firefox = apply()
}

class Firefox private () extends SeleniumBrowser {
  def name: String = "Firefox"

  def newDriver: BrowserDriver = new FirefoxDriver

  private class FirefoxDriver extends BrowserDriver {
    protected def newDriver(): RemoteWebDriver =
      new org.openqa.selenium.firefox.FirefoxDriver()
  }
}
