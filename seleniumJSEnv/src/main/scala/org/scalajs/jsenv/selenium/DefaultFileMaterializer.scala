package org.scalajs.jsenv.selenium

import org.scalajs.core.tools.io.VirtualTextFile
import org.scalajs.jsenv.VirtualFileMaterializer
import java.net.URL

/** Materializes virtual files in a temporary directory and links to them
  * via file://
  */
object DefaultFileMaterializer extends FileMaterializer {

  private val materializer = new VirtualFileMaterializer(true)

  override def materialize(vf: VirtualTextFile): URL = {
    materializer.materialize(vf).toURI.toURL
  }
}
