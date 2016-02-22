#! /bin/sh

sbt seleniumJSEnvTest/run 'set scalaJSStage in Global := FullOptStage' seleniumJSEnvTest/run \
  reload 'set inScope(ThisScope in seleniumJSEnvTest)(jsEnv := new org.scalajs.jsenv.selenium.SeleniumJSEnv().withBrowser(org.scalajs.jsenv.selenium.Chrome))' seleniumJSEnvTest/run 'set scalaJSStage in Global := FullOptStage' seleniumJSEnvTest/run \
  reload seleniumJSEnvTest/test 'set scalaJSStage in Global := FullOptStage' seleniumJSEnvTest/test \
  reload 'set inScope(ThisScope in seleniumJSEnvTest)(jsEnv := new org.scalajs.jsenv.selenium.SeleniumJSEnv().withBrowser(org.scalajs.jsenv.selenium.Chrome))' seleniumJSEnvTest/test 'set scalaJSStage in Global := FullOptStage' seleniumJSEnvTest/test
