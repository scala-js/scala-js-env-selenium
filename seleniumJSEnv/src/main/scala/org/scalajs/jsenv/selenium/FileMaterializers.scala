package org.scalajs.jsenv.selenium

import java.io._
import java.nio.file._
import java.net._

import org.scalajs.core.tools.io._
import org.scalajs.jsenv.VirtualFileMaterializer

private[selenium] trait FileMaterializer {
  def materialize(vf: VirtualTextFile): URL
}

/** Materializes virtual files in a temp directory (uses file:// schema). */
private[selenium] object TempDirFileMaterializer extends FileMaterializer {
  private val materializer = new VirtualFileMaterializer(singleDir = false)

  override def materialize(vf: VirtualTextFile): URL = {
    materializer.materialize(vf).toURI.toURL
  }
}

private[selenium] class ServerDirFileMaterializer(contentDir: Path,
    webRoot: URL) extends FileMaterializer {

  def this(contentDir: String, webRoot: String) =
    this(Paths.get(contentDir), new URL(webRoot))

  require(webRoot.getPath().endsWith("/"), "webRoot must end with a slash (/)")

  Files.createDirectories(contentDir)

  override def materialize(vf: VirtualTextFile): URL = {
    val f = contentDir.resolve(vf.name).toFile
    f.deleteOnExit()
    IO.copyTo(vf, WritableFileVirtualTextFile(f))

    val nameURI = new URI(null, null, vf.name, null)
    webRoot.toURI.resolve(nameURI).toURL
  }
}
