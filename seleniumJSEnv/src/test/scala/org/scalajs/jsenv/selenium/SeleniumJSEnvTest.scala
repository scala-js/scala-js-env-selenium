package org.scalajs.jsenv.selenium

import org.scalajs.jsenv.test._
import org.junit._

abstract class SeleniumJSEnvTest extends TimeoutComTests {
  // Additional tests. Should probably be included upstream.

  @Test // #74
  def closeAfterStartTest: Unit = {
    val runner = asyncRunner("")
    start(runner)
    runner.stop()
    runner.await()
  }

  @Test // #79
  def throwExceptionTest: Unit = {
    """
    throw 1;
    """.fails()
  }

  @Test // #79
  def catchExceptionTest: Unit = {
    """
    try {
      throw "hello world";
    } catch (e) {
      console.log(e);
    }
    """ hasOutput "hello world\n"
  }

  /* We need to ignore the timeout tests, since we are not able to implement
   * "smart termination". In fact, this requirement is going to be dropped in
   * JSEnvs because in general, JS VMs do not support it (see #55).
   */

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
}

class SeleniumJSEnvChromeTest extends SeleniumJSEnvTest {
  import org.openqa.selenium.chrome.ChromeOptions

  protected def newJSEnv: SeleniumJSEnv = new SeleniumJSEnv(new ChromeOptions())
}

class SeleniumJSEnvFirefoxTest extends SeleniumJSEnvTest {
  import org.openqa.selenium.firefox.FirefoxOptions

  protected def newJSEnv: SeleniumJSEnv = new SeleniumJSEnv(new FirefoxOptions())
}
