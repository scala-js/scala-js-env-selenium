package org.scalajs.jsenv.selenium.chrome

import org.openqa.selenium.remote.DesiredCapabilities

import org.scalajs.jsenv.selenium.SeleniumJSEnv
import org.scalajs.jsenv.test._

import org.junit._

class SeleniumJSEnvChromeTest extends TimeoutComTests {
  protected def newJSEnv: SeleniumJSEnv =
    new SeleniumJSEnv(DesiredCapabilities.chrome())

  @Ignore("Stop does not work properly, issue #54.")
  @Test override def stopTestCom: Unit = super.stopTestCom

  @Ignore("Stop does not work properly, issue #54.")
  @Test override def futureStopTest: Unit = super.futureStopTest

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
}
