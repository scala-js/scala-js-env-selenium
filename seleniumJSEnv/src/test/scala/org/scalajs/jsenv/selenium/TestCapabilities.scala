package org.scalajs.jsenv.selenium

import org.openqa.selenium.Capabilities
import org.openqa.selenium.firefox.{FirefoxOptions, FirefoxDriverLogLevel}
import org.openqa.selenium.chrome.ChromeOptions

import java.util.logging.{Logger, Level}

object TestCapabilities {
  // Lower the logging level for Selenium to avoid spam.
  Logger.getLogger("org.openqa.selenium").setLevel(Level.WARNING)

  def fromEnv: Capabilities = sys.env.getOrElse("SJS_TEST_BROWSER", "firefox") match {
    case "firefox" =>
      new FirefoxOptions()
        .setHeadless(true)
        .setLogLevel(FirefoxDriverLogLevel.ERROR)

    case "chrome" =>
      new ChromeOptions()
        .setHeadless(true)

    case name =>
      throw new IllegalArgumentException(s"Unknown browser $name")
  }
}
