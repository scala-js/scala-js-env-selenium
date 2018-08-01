package org.scalajs.jsenv.selenium

import scala.scalajs.js.Dynamic._

// Canvas example from: http://www.w3schools.com/html/html5_canvas.asp
object CanvasCreator {

  def create(id: String): Unit = {
    ElementCreator.create("canvas", id = id)
  }

  def paint(id: String): Unit = {
    val canvas = global.document.getElementById(id)
    val ctx = canvas.getContext("2d")

    val grd = ctx.createLinearGradient(0, 0, 200, 0)
    grd.addColorStop(0, "red")
    grd.addColorStop(1, "white")

    ctx.fillStyle = grd
    ctx.fillRect(10, 10, 150, 80)
  }
}
