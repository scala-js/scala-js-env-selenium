package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io._

trait SeleniumBrowser {
  def newDriver: BrowserDriver
  def name: String
  def initFiles(): Seq[VirtualJSFile] = Nil
}
