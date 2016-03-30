package org.scalajs.jsenv.selenium

import java.io.File
import java.net.URL

import org.scalajs.core.tools.io.{IO, VirtualTextFile, WritableFileVirtualTextFile}

/** Materializes files on the filesystem and specifies a custom url to access stored files.
 *  This can be used to bypass cross origin access policies as shown below.
 *
 *  @param fsRoot Location on the filesystem where to store the generated files
 *  @param webRoot Corresponding url to access the files
 *
 *  @example
 *
 *  The following illustrates how to configure a project such that the browser fetches
 *  files by http:// instead of file://.
 *  This example assumes a local webserver is running and serving the ".tmp"
 *  directory at http://localhost:8080
 *
 *  <pre>
 *    jsSettings(
 *      // ...
 *      jsEnv := new SeleniumJSEnv(org.scalajs.jsenv.selenium.Firefox)
 *          .withMaterializer(new SpecificFileMaterializer(".tmp", "http://localhost:8080"))
 *    )
 *  </pre>
 */
class CustomFileMaterializer(val fsRoot: String, val webRoot: String) extends FileMaterializer {

  val storageDir = createStorageDir()

  /** Create a target file to write/copy to. Will also call
    *  deleteOnExit on the file.
    */
  private def trgFile(name: String): File = {
    val f = new File(storageDir, name)
    f.deleteOnExit()
    f
  }

  /** Creates the storage directory if it does not exist. */
  private def createStorageDir(): File = {
    val storageDir = new File(fsRoot)
    storageDir.mkdir()
    storageDir
  }

  override def materialize(vf: VirtualTextFile): URL = {
    val trg = trgFile(vf.name)
    IO.copyTo(vf, WritableFileVirtualTextFile(trg))
    new URL(webRoot + "/" + vf.name)
  }

}
