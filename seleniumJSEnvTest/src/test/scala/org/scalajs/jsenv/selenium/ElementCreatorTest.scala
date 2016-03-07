package org.scalajs.jsenv.selenium

import org.junit.Assert._
import org.junit.Test

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

class ElementCreatorTest {

  @Test def should_be_able_to_create_an_element_in_the_body(): Unit = {
    // create the element
    ElementCreator.create("<h1>Testing DOM.</h1>")

    // jquery would make this easier, but I wanted to
    // only use pure html in the test itself
    val body = global.document.getElementsByTagName("body")
      .asInstanceOf[js.Array[js.Dynamic]].head

    // the Scala.js DOM API would make this easier
    assertEquals("H1", body.lastChild.tagName.toString)
    assertEquals("Testing DOM.", body.lastChild.innerHTML.toString)
  }
}
