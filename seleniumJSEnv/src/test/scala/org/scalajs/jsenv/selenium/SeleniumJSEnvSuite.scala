package org.scalajs.jsenv.selenium

import org.scalajs.jsenv.test._
import org.junit.runner.RunWith

@RunWith(classOf[JSEnvSuiteRunner])
class SeleniumJSSuite extends JSEnvSuite(
  JSEnvSuiteConfig(new SeleniumJSEnv(TestCapabilities.fromEnv))
)
