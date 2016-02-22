package org.scalajs.jsenv.selenium

import org.junit.Assert._
import org.junit.Test

import scala.scalajs.js.Dynamic.global
import scala.scalajs.js.isUndefined

class DocumentTest {

  @Test def document(): Unit = {
    assertFalse(isUndefined(global.document))
    assertEquals("#document", global.document.nodeName)
  }

  @Test def documentBody(): Unit = {
    assertFalse(isUndefined(global.document.body))
  }
}
