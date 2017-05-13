package org.scalajs.jsenv.selenium.firefox

import org.scalajs.jsenv.selenium.{Firefox, SeleniumJSEnv}
import org.scalajs.jsenv.test._

import org.junit._

class SeleniumJSEnvFirefoxTest extends TimeoutComTests {
  protected def newJSEnv: SeleniumJSEnv = new SeleniumJSEnv(Firefox())

  @Ignore("Not waiting for event loop to finish, issue #55.")
  @Test
  override def basicTimeoutTest: Unit = super.basicTimeoutTest

  @Ignore("Not waiting for event loop to finish, issue #55.")
  @Test
  override def clearTimeoutTest: Unit = super.clearTimeoutTest

  @Ignore("Not waiting for event loop to finish, issue #55.")
  @Test
  override def timeoutArgTest: Unit = super.timeoutArgTest

  @Ignore("Not waiting for event loop to finish, issue #55.")
  @Test
  override def intervalSelfClearTest: Unit = super.intervalSelfClearTest

  @Ignore("Not waiting for event loop to finish, issue #55.")
  @Test
  override def intervalTest: Unit = super.intervalTest

  @Ignore("Not waiting for event loop to finish, issue #55.")
  @Test
  override def receiveTimeoutTest: Unit = super.receiveTimeoutTest

  @Ignore("W3C WebDriver spec lacks log interface, issue #64")
  @Test
  override def timeoutSingleArgTest: Unit = super.timeoutArgTest

  @Ignore("W3C WebDriver spec lacks log interface, issue #64")
  @Test // Failed in Phantom - #2053
  override def utf8Test: Unit = super.utf8Test

  @Ignore("W3C WebDriver spec lacks log interface, issue #64")
  @Test
  override def allowScriptTags: Unit = super.allowScriptTags

}
