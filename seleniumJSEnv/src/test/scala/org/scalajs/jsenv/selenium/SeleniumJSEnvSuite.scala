package org.scalajs.jsenv.selenium

import java.util.Arrays

import org.scalajs.jsenv.test._

import org.junit.runner.RunWith
import org.junit.runner.Runner
import org.junit.runners.Suite
import org.junit.runner.manipulation.Filter
import org.junit.runner.Description

@RunWith(classOf[SeleniumJSSuiteRunner])
class SeleniumJSSuite extends JSEnvSuite(
  JSEnvSuiteConfig(new SeleniumJSEnv(TestCapabilities.fromEnv))
)

class SeleniumJSSuiteRunner private (
    root: Class[_], base: JSEnvSuiteRunner)
    extends Suite(root, Arrays.asList[Runner](base)) {

  /** Constructor for reflective instantiation via `@RunWith`. */
  def this(suite: Class[_ <: SeleniumJSSuite]) =
    this(suite, new JSEnvSuiteRunner(suite))

  // Ignore `largeMessageTest` for chrome.
  if (TestCapabilities.nameFromEnv == "chrome") {
    base.filter(new Filter {
      def describe(): String = "Ignore largeMessageTest"

      def shouldRun(description: Description): Boolean = {
        description.getMethodName == null ||
        !description.getMethodName.startsWith("largeMessageTest")
      }
    })
  }
}
