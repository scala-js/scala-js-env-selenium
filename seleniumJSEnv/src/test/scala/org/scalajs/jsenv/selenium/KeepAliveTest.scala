package org.scalajs.jsenv.selenium

import scala.concurrent.Await
import scala.concurrent.duration._

import java.net.URL

import org.openqa.selenium._
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.server._

import org.junit._
import org.junit.Assert._

import org.scalajs.jsenv._

class KeepAliveTest {
  private final class MockWebDriver extends WebDriver with JavascriptExecutor {
    var closed = false

    def close(): Unit = closed = true
    def quit(): Unit = closed = true

    def executeScript(code: String, args: Object*): Object =
      new java.util.HashMap[String, java.util.List[String]]()

    def get(url: String): Unit = ()

    def navigate(): WebDriver.Navigation = new WebDriver.Navigation {
      def back(): Unit = ()
      def forward(): Unit = ()
      def refresh(): Unit = ()
      def to(url: String): Unit = ()
      def to(url: URL): Unit = ()
    }

    // Stuff we won't need.

    def executeAsyncScript(code: String, args: Object*): Object = ???

    def findElement(x1: By): WebElement = ???
    def findElements(x1: By): java.util.List[WebElement] = ???

    def getTitle(): String = ???
    def getWindowHandle(): String = ???
    def getWindowHandles(): java.util.Set[String] = ???
    def manage(): WebDriver.Options = ???

    def getCurrentUrl(): String = ???
    def getPageSource(): String = ???

    def switchTo(): WebDriver.TargetLocator = ???
  }

  private final class MockInjector(driver: WebDriver) extends DriverFactory {
    var used = false

    def newInstance(caps: Capabilities): WebDriver = {
      require(!used)
      used = true
      driver
    }

    def hasMappingFor(caps: Capabilities): Boolean = true
    def registerDriverProvider(p: DriverProvider): Unit = ???
  }

  private def setup(keepAlive: Boolean) = {
    val driver = new MockWebDriver
    val factory = new MockInjector(driver)
    val config = SeleniumJSEnv.Config()
      .withDriverFactory(factory)
      .withKeepAlive(keepAlive)
    val env = new SeleniumJSEnv(new DesiredCapabilities, config)

    (driver, factory, env)
  }

  private def runNoCom(env: JSEnv) = {
    val run = env.start(Nil, RunConfig())
    run.close()
    Await.ready(run.future, 1.minute)
  }

  private def runWithCom(env: JSEnv) = {
    val run = env.startWithCom(Nil, RunConfig(), _ => ())
    run.close()
    Await.ready(run.future, 1.minute)
  }

  @Test
  def runClosesWithoutKeepAlive: Unit = {
    val (driver, factory, env) = setup(keepAlive = false)
    runNoCom(env)
    assertTrue(factory.used)
    assertTrue(driver.closed)
  }

  @Test
  def runNoCloseWithKeepAlive: Unit = {
    val (driver, factory, env) = setup(keepAlive = true)
    runNoCom(env)
    assertTrue(factory.used)
    assertFalse(driver.closed)
  }

  @Test
  def comRunClosesWithoutKeepAlive: Unit = {
    val (driver, factory, env) = setup(keepAlive = false)
    runWithCom(env)
    assertTrue(factory.used)
    assertTrue(driver.closed)
  }

  @Test
  def comRunNoCloseWithKeepAlive: Unit = {
    val (driver, factory, env) = setup(keepAlive = true)
    runWithCom(env)
    assertTrue(factory.used)
    assertFalse(driver.closed)
  }
}
