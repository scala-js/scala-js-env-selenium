package org.scalajs.jsenv.selenium

import org.junit.Assert._
import org.junit.Test

import scala.scalajs.js.Dynamic.global
import scala.scalajs.js.isUndefined

class WindowTest {

  @Test def WindowTest(): Unit = {
     assertFalse(isUndefined(global.window))
  }
}
