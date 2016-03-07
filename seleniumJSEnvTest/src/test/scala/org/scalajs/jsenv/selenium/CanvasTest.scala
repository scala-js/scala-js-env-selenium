package org.scalajs.jsenv.selenium

import org.junit.Assert._
import org.junit.Test

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

class CanvasTest {

  @Test def testCanvas(): Unit = {
    // create the element
    val canvasId = "testCanvas"
    CanvasCreator.create(canvasId)
    CanvasCreator.paint(canvasId)

    val body = global.document.getElementsByTagName("body")
        .asInstanceOf[js.Array[js.Dynamic]].head

    assertEquals("canvas", body.lastChild.tagName.toString.toLowerCase)
  }
}
