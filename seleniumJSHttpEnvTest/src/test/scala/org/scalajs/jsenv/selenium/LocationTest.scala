package org.scalajs.jsenv.selenium

import org.junit.Assert._
import org.junit.Test

import scala.scalajs.js.Dynamic.global

class LocationTest {

  @Test def LocationTest(): Unit = {
    assertEquals("http:", global.window.location.protocol.toString())
    assertEquals("localhost:8080", global.window.location.host.toString())
  }
}
