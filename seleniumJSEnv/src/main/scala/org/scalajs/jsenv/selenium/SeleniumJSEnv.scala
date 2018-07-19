package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io.VirtualJSFile
import org.scalajs.core.tools.jsdep.ResolvedJSDependency

import org.openqa.selenium._
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.server._

import org.scalajs.jsenv._

import scala.reflect.{ClassTag, classTag}

import java.net.URL
import java.nio.file.{Path, Paths}

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
      val materialization: Config.Materialization
  ) {
    import Config.Materialization

    private def this() = this(
        keepAlive = false,
        materialization = Config.Materialization.Temp,
        driverFactory = new DefaultDriverFactory(Platform.getCurrent()))

    /** Materializes purely virtual files into a temp directory.
     *
     *  Materialization is necessary so that virtual files can be referred to by
     *  name. If you do not know/care how your files are referred to, this is a
     *  good default choice. It is also the default of [[SeleniumJSEnv.Config]].
     */
    def withMaterializeInTemp: Config =
      copy(materialization = Materialization.Temp)

    /** Materializes files in a static directory of a user configured server.
     *
     *  This can be used to bypass cross origin access policies.
     *
     *  @param contentDir Static content directory of the server. The files will
     *      be put here. Will get created if it doesn't exist.
     *  @param webRoot URL making `contentDir` accessible thorugh the server.
     *      This must have a trailing slash to be interpreted as a directory.
     *
     *  @example
     *
     *  The following will make the browser fetch files using the http:// schema
     *  instead of the file:// schema. The example assumes a local webserver is
     *  running and serving the ".tmp" directory at http://localhost:8080.
     *
     *  {{{
     *  jsSettings(
     *    jsEnv := new SeleniumJSEnv(
     *        new org.openqa.selenium.firefox.FirefoxOptions(),
     *        SeleniumJSEnv.Config()
     *          .withMaterializeInServer(".tmp", "http://localhost:8080/")
     *    )
     *  )
     *  }}}
     */
    def withMaterializeInServer(contentDir: String, webRoot: String): Config =
      withMaterializeInServer(Paths.get(contentDir), new URL(webRoot))

    /** Materializes files in a static directory of a user configured server.
     *
     *  Version of `withMaterializeInServer` with stronger typing.
     *
     *  @param contentDir Static content directory of the server. The files will
     *      be put here. Will get created if it doesn't exist.
     *  @param webRoot URL making `contentDir` accessible thorugh the server.
     *      This must have a trailing slash to be interpreted as a directory.
     */
    def withMaterializeInServer(contentDir: Path, webRoot: URL): Config =
      copy(materialization = Materialization.Server(contentDir, webRoot))

    def withMaterialization(materialization: Materialization): Config =
      copy(materialization = materialization)

    def withKeepAlive(keepAlive: Boolean): Config =
      copy(keepAlive = keepAlive)

    def withDriverFactory(driverFactory: DriverFactory): Config =
      copy(driverFactory = driverFactory)

    @deprecated("Use materialization instead", "0.2.1")
    lazy val materializer: FileMaterializer = newMaterializer

    private[selenium] def newMaterializer: FileMaterializer = materialization match {
      case Materialization.Temp =>
        TempDirFileMaterializer

      case Materialization.Server(contentDir, webRoot) =>
        new ServerDirFileMaterializer(contentDir, webRoot)
    }

    private def copy(keepAlive: Boolean = keepAlive,
        materialization: Config.Materialization = materialization,
        driverFactory: DriverFactory = driverFactory) = {
      new Config(driverFactory, keepAlive, materialization)
    }
  }

  object Config {
    def apply(): Config = new Config()

    abstract class Materialization private ()
    object Materialization {
      final case object Temp extends Materialization
      final case class Server(contentDir: Path, webRoot: URL) extends Materialization {
        require(webRoot.getPath().endsWith("/"), "webRoot must end with a slash (/)")
      }
    }
  }
}
