package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io.VirtualJSFile
import org.scalajs.core.tools.jsdep.ResolvedJSDependency

import org.openqa.selenium._
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.server._

import org.scalajs.jsenv._

import scala.reflect.{ClassTag, classTag}

class SeleniumJSEnv(capabilities: Capabilities, config: SeleniumJSEnv.Config)
    extends AsyncJSEnv with ComJSEnv {

  def this(capabilities: Capabilities) =
    this(capabilities, SeleniumJSEnv.Config())

  private val augmentedCapabilities = {
    val x = new DesiredCapabilities(capabilities)
    x.setJavascriptEnabled(true)
    x
  }

  def name: String = s"SeleniumJSEnv ($capabilities)"

  def jsRunner(libs: Seq[ResolvedJSDependency], code: VirtualJSFile): JSRunner =
    new SeleniumRunner(newDriver _, libs, code, config)

  def asyncRunner(libs: Seq[ResolvedJSDependency],
      code: VirtualJSFile): AsyncJSRunner = {
    new SeleniumAsyncJSRunner(newDriver _, libs, code, config)
  }

  def comRunner(libs: Seq[ResolvedJSDependency],
      code: VirtualJSFile): ComJSRunner = {
    new SeleniumComJSRunner(newDriver _, libs, code, config)
  }

  private def newDriver() = {
    val driver: WebDriver =
      config.driverFactory.newInstance(augmentedCapabilities)

    /* The first `asInstanceOf`s are a fail-fast for the second one, which
     * scalac partially erases, so that we're sure right now that the last
     * cast is correct, as opposed to crashing when we call a method of
     * JavascriptExecutor on the driver.
     *
     * We are "allowed" to cast since we explicitly request JavascriptEnabled in
     * the capabilities.
     */
    driver.asInstanceOf[JavascriptExecutor]

    driver.asInstanceOf[WebDriver with JavascriptExecutor]
  }
}

object SeleniumJSEnv {
  final class Config private (
      val driverFactory: DriverFactory,
      val keepAlive: Boolean,
      val materializer: FileMaterializer
  ) {
    private def this() = this(
        keepAlive = false,
        materializer = DefaultFileMaterializer,
        driverFactory = Config.defaultFactory)

    def withMaterializer(materializer: FileMaterializer): Config =
      copy(materializer = materializer)

    def withKeepAlive(keepAlive: Boolean): Config =
      copy(keepAlive = keepAlive)

    def withDriverFactory(driverFactory: DriverFactory): Config =
      copy(driverFactory = driverFactory)

    private def copy(keepAlive: Boolean = keepAlive,
        materializer: FileMaterializer = materializer,
        driverFactory: DriverFactory = driverFactory) = {
      new Config(driverFactory, keepAlive, materializer)
    }
  }

  object Config {
    private val defaultFactory = {
      val factory = new DefaultDriverFactory()

      def r(caps: Capabilities, clazz: Class[_ <: WebDriver]) =
        factory.registerDriverProvider(new DefaultDriverProvider(caps, clazz))

      import org.openqa.{selenium => s}

      r(DesiredCapabilities.firefox(), classOf[s.firefox.FirefoxDriver])
      r(DesiredCapabilities.chrome(), classOf[s.chrome.ChromeDriver])
      r(DesiredCapabilities.internetExplorer(), classOf[s.ie.InternetExplorerDriver])
      r(DesiredCapabilities.edge(), classOf[s.edge.EdgeDriver])
      r(DesiredCapabilities.operaBlink(), classOf[s.opera.OperaDriver])
      r(DesiredCapabilities.safari(), classOf[s.safari.SafariDriver])
      r(DesiredCapabilities.phantomjs(), classOf[s.phantomjs.PhantomJSDriver])
      r(DesiredCapabilities.htmlUnit(), classOf[s.htmlunit.HtmlUnitDriver])

      factory
    }

    def apply(): Config = new Config()
  }
}
