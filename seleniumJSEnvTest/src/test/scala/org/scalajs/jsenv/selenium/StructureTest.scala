package org.scalajs.jsenv.selenium

import org.junit.Assert._
import org.junit.Test

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

class StructureTest {
  private val canvasId = "testCanvas"

  @Test
  def testStructure(): Unit = {
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
}
