package org.scalajs.jsenv.selenium

import org.openqa.selenium._
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.server._

import org.junit._
import org.junit.Assert._

import org.scalajs.jsenv._
import org.scalajs.core.tools.io.MemVirtualJSFile
import org.scalajs.core.tools.logging.NullLogger

class KeepAliveTest {
  class MockWebDriver extends WebDriver with JavascriptExecutor {
    var closed = false

    def close(): Unit = closed = true
    def quit(): Unit = closed = true

    def executeScript(code: String, args: Object*): Object = null

    def get(url: String): Unit = ()

    // Stuff we won't need.

    def executeAsyncScript(code: String, args: Object*): Object = ???

    def findElement(x1: By): WebElement = ???
    def findElements(x1: By): java.util.List[WebElement] = ???

    def getTitle(): String = ???
    def getWindowHandle(): String = ???
    def getWindowHandles(): java.util.Set[String] = ???
    def manage(): WebDriver.Options = ???
    def navigate(): WebDriver.Navigation = ???

    def getCurrentUrl(): String = ???
    def getPageSource(): String = ???

    def switchTo(): WebDriver.TargetLocator = ???
  }

  private def newEnv(driver: WebDriver, keepAlive: Boolean): SeleniumJSEnv = {
    DriverInjector.inject(driver,
        SeleniumJSEnv.Config().withKeepAlive(keepAlive))
  }

  private def runSync(env: SeleniumJSEnv): Unit = {
    env.jsRunner(Nil, new MemVirtualJSFile("no_script.js"))
      .run(NullLogger, NullJSConsole)
  }

  private def runAsync(env: SeleniumJSEnv): Unit = {
    val runner = env.asyncRunner(Nil, new MemVirtualJSFile("no_script.js"))
    runner.start(NullLogger, NullJSConsole)
    runner.await()
  }

  private def runCom(env: SeleniumJSEnv): Unit = {
    val runner = env.comRunner(Nil, new MemVirtualJSFile("no_script.js"))
    runner.start(NullLogger, NullJSConsole)
    runner.stop()
    runner.await()
  }

  @Test
  def runClosesWithoutKeepAlive: Unit = {
    val driver = new MockWebDriver()
    runSync(newEnv(driver, keepAlive = false))
    assertTrue(driver.closed)
  }

  @Test
  def runNoCloseWithKeepAlive: Unit = {
    val driver = new MockWebDriver()
    runSync(newEnv(driver, keepAlive = true))
    assertFalse(driver.closed)
  }

  @Test
  @Ignore("Async doesn't close - #73")
  def asyncRunClosesWithoutKeepAlive: Unit = {
    val driver = new MockWebDriver()
    runAsync(newEnv(driver, keepAlive = false))
    assertTrue(driver.closed)
  }

  @Test
  def asyncRunNoCloseWithKeepAlive: Unit = {
    val driver = new MockWebDriver()
    runAsync(newEnv(driver, keepAlive = true))
    assertFalse(driver.closed)
  }

  @Test
  def comRunClosesWithoutKeepAlive: Unit = {
    val driver = new MockWebDriver()
    runCom(newEnv(driver, keepAlive = false))
    assertTrue(driver.closed)
  }

  @Test
  def comRunNoCloseWithKeepAlive: Unit = {
    val driver = new MockWebDriver()
    runCom(newEnv(driver, keepAlive = true))
    assertFalse(driver.closed)
  }
}
