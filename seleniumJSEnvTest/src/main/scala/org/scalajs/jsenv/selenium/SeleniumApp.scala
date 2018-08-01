package org.scalajs.jsenv.selenium

import scalajs.js
import scalajs.js.Dynamic._
import scalajs.js.annotation.JSExport

object SeleniumApp extends js.JSApp {
  private val canvasId = "testCanvas"

  def main(): Unit = {
    ElementCreator.create("h1", text = "SeleniumJSEnv Test")
    CanvasCreator.create(canvasId)
    CanvasCreator.paint(canvasId)

    assertStructure()

    ElementCreator.create("h1", text = "'run' finished.")
    println("'run' finished.")
  }

  def assertStructure(): Unit = {
    val nodes = global.document.body.childNodes.asInstanceOf[js.Array[js.Dynamic]]
    val elements = nodes.takeRight(2) // Ignore scripts

    val h1 = elements(0)
    assertEquals("H1", h1.tagName.asInstanceOf[String])
    assertEquals("SeleniumJSEnv Test", h1.textContent.asInstanceOf[String])

    val canvas = elements(1)
    assertEquals("CANVAS", canvas.tagName.asInstanceOf[String])
    assertEquals(canvasId, canvas.id.asInstanceOf[String])
  }

  def assertEquals(expected: String, actual: String): Unit = {
    if (expected != actual)
      throw new Exception(s"Expected <$expected> but got <$actual>")
  }
}
