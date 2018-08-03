package org.scalajs.jsenv.selenium

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

object ElementCreator {
  def create(element: String, id: String = "", text: String = ""): js.Dynamic = {
    val el = global.document.createElement(element)

    if (id != "") {
      el.id = id
    }

    if (text != "") {
      el.textContent = text
    }

    global.document.body.appendChild(el)
    el
  }
}
