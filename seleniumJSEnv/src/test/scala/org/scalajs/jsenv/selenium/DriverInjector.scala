package org.scalajs.jsenv.selenium

import org.openqa.selenium._
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.server._

/** A helper class to inject a given WebDriver into a SeleniumJSEnv */
object DriverInjector {
  private class DriverInjector(driver: WebDriver) extends DriverFactory {
    private[this] var used = false

    def newInstance(caps: Capabilities): WebDriver = {
      require(!used)
      used = true
      driver
    }

    def hasMappingFor(caps: Capabilities): Boolean = ???
    def registerDriverProvider(p: DriverProvider): Unit = ???
  }

  def inject(driver: WebDriver, config: SeleniumJSEnv.Config): SeleniumJSEnv = {
    val factory = new DriverInjector(driver)
    new SeleniumJSEnv(new DesiredCapabilities(),
        config.withDriverFactory(factory))
  }
}
