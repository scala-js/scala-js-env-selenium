package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io.VirtualJSFile
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.jsenv.{AsyncJSEnv, ComJSEnv}
import org.scalajs.jsenv.{JSRunner, AsyncJSRunner, ComJSRunner}

class SeleniumJSEnv private (browser: SeleniumBrowser, keepAlive: Boolean, materializer: FileMaterializer)
    extends AsyncJSEnv with ComJSEnv {

  def this(browser: SeleniumBrowser) =
    this(browser, keepAlive = false, materializer = DefaultFileMaterializer)

  def withMaterializer(materializer: FileMaterializer): SeleniumJSEnv =
    new SeleniumJSEnv(browser, keepAlive, materializer)

  def withKeepAlive(): SeleniumJSEnv =
    new SeleniumJSEnv(browser, keepAlive = true, materializer)

  def browserName: String = browser.name

  def name: String = "SeleniumJSEnv for " + browserName

  def jsRunner(libs: Seq[ResolvedJSDependency], code: VirtualJSFile): JSRunner =
    new SeleniumRunner(browser, libs, code, keepAlive, materializer)

  def asyncRunner(libs: Seq[ResolvedJSDependency],
      code: VirtualJSFile): AsyncJSRunner = {
    new SeleniumAsyncJSRunner(browser, libs, code, keepAlive, materializer)
  }

  def comRunner(libs: Seq[ResolvedJSDependency],
      code: VirtualJSFile): ComJSRunner = {
    new SeleniumComJSRunner(browser, libs, code, keepAlive, materializer)
  }
}
