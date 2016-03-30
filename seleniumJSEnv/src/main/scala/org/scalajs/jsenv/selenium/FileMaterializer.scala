package org.scalajs.jsenv.selenium

import java.net.URL

import org.scalajs.core.tools.io.VirtualTextFile

trait FileMaterializer {
  def materialize(vf: VirtualTextFile): URL
}
