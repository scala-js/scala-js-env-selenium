package org.scalajs.jsenv.selenium

import org.junit.Assert._
import org.junit.Test

class ModuleSupportTest {
  @Test def testBasicImport(): Unit = {
    assertEquals("Hello scalaJsSelenium!", CamelCase.hello("scala js selenium"))
  }
}
