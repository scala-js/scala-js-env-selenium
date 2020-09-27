package org.scalajs.jsenv.selenium

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object CamelCase {
  def hello(input: String): String = s"Hello ${camelCase(input)}!"

  @JSImport("https://cdn.skypack.dev/camelcase@^6.0.0", JSImport.Default)
  @js.native
  def camelCase(input: String): String = js.native
}
