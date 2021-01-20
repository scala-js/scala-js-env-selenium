package org.scalajs.jsenv.selenium

import java.util.Arrays

import org.scalajs.jsenv.test._

import org.junit.runner.RunWith
import org.junit.runner.Runner
import org.junit.runners.Suite
import org.junit.runner.manipulation.Filter
import org.junit.runner.Description

@RunWith(classOf[JSEnvSuiteRunner])
class SeleniumJSSuite extends JSEnvSuite(
  JSEnvSuiteConfig(new SeleniumJSEnv(TestCapabilities.fromEnv))
)
