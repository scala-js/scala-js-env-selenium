package org.scalajs.jsenv.selenium

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

object ElementCreator {
  val jQ = global.jQuery

  def create(element: String): js.Dynamic = jQ("body").append(jQ(element))
}
