import sbt.Keys._

import org.scalajs.jsenv.selenium.SeleniumJSEnv
import org.scalajs.jsenv.selenium.Firefox

val commonSettings: Seq[Setting[_]] = Seq(
  version := "0.1.1-SNAPSHOT",
  organization := "org.scala-js",
  scalaVersion := "2.11.7",
  scalacOptions ++= Seq("-deprecation", "-feature", "-Xfatal-warnings"),

  homepage := Some(url("http://scala-js.org/")),
  licenses += ("BSD New",
      url("https://github.com/scala-js/scala-js-env-selenium/blob/master/LICENSE")),
  scmInfo := Some(ScmInfo(
      url("https://github.com/scala-js/scala-js-env-selenium"),
      "scm:git:git@github.com:scala-js/scala-js-env-selenium.git",
      Some("scm:git:git@github.com:scala-js/scala-js-env-selenium.git")))
)

lazy val seleniumJSEnv: Project = project.
  settings(commonSettings).
  settings(
    libraryDependencies ++= Seq(
        "org.scala-js" %% "scalajs-js-envs" % "0.6.7",
        "org.seleniumhq.selenium" % "selenium-java" % "2.49.1",
        "org.seleniumhq.selenium" % "selenium-chrome-driver" % "2.49.1"
    ),

    publishMavenStyle := true,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    pomExtra := (
      <developers>
        <developer>
          <id>nicolasstucki</id>
          <name>Nicolas Stucki</name>
          <url>https://github.com/nicolasstucki/</url>
        </developer>
        <developer>
          <id>sjrd</id>
          <name>Sébastien Doeraene</name>
          <url>https://github.com/sjrd/</url>
        </developer>
        <developer>
          <id>gzm0</id>
          <name>Tobias Schlatter</name>
          <url>https://github.com/gzm0/</url>
        </developer>
      </developers>
    ),
    pomIncludeRepository := { _ => false }
  )

lazy val seleniumJSEnvTest: Project = project.
  enablePlugins(ScalaJSPlugin).
  enablePlugins(ScalaJSJUnitPlugin).
  settings(commonSettings).
  settings(
    testOptions +=
      Tests.Argument(TestFramework("com.novocode.junit.JUnitFramework"), "-v", "-a"),
    jsDependencies ++= Seq(
        RuntimeDOM % "test",
        "org.webjars" % "jquery" % "1.10.2" / "jquery.js"
    ),
    jsEnv := new SeleniumJSEnv(Firefox)
  )
