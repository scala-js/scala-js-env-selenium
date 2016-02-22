package org.scalajs.jsenv.selenium

import org.junit.Assert._
import org.junit.Test

import scala.scalajs.js.Dynamic.global
import scala.scalajs.js.isUndefined

class JQueryTest {

  @Test def jQuery(): Unit = {
    assertFalse(isUndefined(global.jQuery))
    assertFalse(isUndefined(global.window.jQuery))
  }

  @Test def `$`(): Unit = {
    assertFalse(isUndefined(global.$))
    assertFalse(isUndefined(global.window.$))
  }
}
