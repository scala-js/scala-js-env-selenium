package org.scalajs.jsenv.selenium

import org.openqa.selenium.Capabilities
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.chrome.ChromeOptions

object TestCapabilities {
  def fromEnv: Capabilities = sys.env.getOrElse("SJS_TEST_BROWSER", "firefox") match {
    case "firefox" => new FirefoxOptions().setHeadless(true)
    case "chrome"  => new ChromeOptions().setHeadless(true)
    case name      => throw new IllegalArgumentException(s"Unknown browser $name")
  }
}
