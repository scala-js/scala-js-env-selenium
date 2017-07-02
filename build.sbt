import sbt.Keys._

import  org.scalajs.sbtplugin.ScalaJSCrossVersion

import org.scalajs.jsenv.selenium.SeleniumJSEnv
import org.scalajs.jsenv.selenium.Firefox
import org.scalajs.jsenv.selenium.CustomFileMaterializer

import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys.{previousArtifact, binaryIssueFilters}

val previousVersion = None

val scalaVersionsUsedForPublishing: Set[String] =
  Set("2.10.6", "2.11.11", "2.12.2")
val newScalaBinaryVersionsInThisRelease: Set[String] =
  Set("2.12")

val commonSettings: Seq[Setting[_]] = Seq(
  version := "0.2.0-SNAPSHOT",
  organization := "org.scala-js",
  scalaVersion := "2.11.11",
  scalacOptions ++= Seq("-deprecation", "-feature", "-Xfatal-warnings"),

  homepage := Some(url("http://scala-js.org/")),
  licenses += ("BSD New",
      url("https://github.com/scala-js/scala-js-env-selenium/blob/master/LICENSE")),
  scmInfo := Some(ScmInfo(
      url("https://github.com/scala-js/scala-js-env-selenium"),
      "scm:git:git@github.com:scala-js/scala-js-env-selenium.git",
      Some("scm:git:git@github.com:scala-js/scala-js-env-selenium.git")))
) ++ mimaDefaultSettings

val previousArtifactSetting: Setting[_] = {
  previousArtifact := {
    val scalaV = scalaVersion.value
    val scalaBinaryV = scalaBinaryVersion.value
    if (!scalaVersionsUsedForPublishing.contains(scalaV)) {
      // This artifact will not be published. Binary compatibility is irrelevant.
      None
    } else if (newScalaBinaryVersionsInThisRelease.contains(scalaBinaryV)) {
      // New in this release, no binary compatibility to comply to
      None
    } else {
      previousVersion.map { pv =>
        val thisProjectID = projectID.value
        /* Filter out e:info.apiURL as it expects 0.6.7-SNAPSHOT, whereas the
         * artifact we're looking for has 0.6.6 (for example).
         */
        val prevExtraAttributes =
          thisProjectID.extraAttributes.filterKeys(_ != "e:info.apiURL")
        val prevProjectID =
          (thisProjectID.organization % thisProjectID.name % pv)
              .cross(thisProjectID.crossVersion)
              .extra(prevExtraAttributes.toSeq: _*)
        CrossVersion(scalaV, scalaBinaryV)(prevProjectID).cross(CrossVersion.Disabled)
      }
    }
  }
}

val baseTestSettings: Seq[Setting[_]] = commonSettings ++ Seq(
  testOptions += Tests.Argument(TestFramework("com.novocode.junit.JUnitFramework"), "-v", "-a")
)

val testSettings: Seq[Setting[_]] = baseTestSettings ++ Seq(
  jsDependencies ++= Seq(
      RuntimeDOM % "test",
      "org.webjars" % "jquery" % "1.10.2" / "jquery.js"
  )
)

// We'll need the name scalajs-env-selenium for the `seleniumJSEnv` project
name := "root"

lazy val seleniumJSEnv: Project = project.
  settings(commonSettings).
  settings(baseTestSettings).
  settings(
    name := "scalajs-env-selenium",

    libraryDependencies ++= Seq(
        "org.scala-js" %% "scalajs-js-envs" % scalaJSVersion,
        "org.seleniumhq.selenium" % "selenium-java" % "2.53.0",
        "org.seleniumhq.selenium" % "selenium-chrome-driver" % "2.53.0",
        "org.scala-js" %% "scalajs-js-envs-test-kit" % scalaJSVersion % "test",
        "com.novocode" % "junit-interface" % "0.11" % "test"
    ),

    previousArtifactSetting,
    binaryIssueFilters ++= BinaryIncompatibilities.SeleniumJSEnv,

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
    pomIncludeRepository := { _ => false },

    parallelExecution in Test := false
  )

lazy val seleniumJSEnvTest: Project = project.
  enablePlugins(ScalaJSPlugin).
  enablePlugins(ScalaJSJUnitPlugin).
  settings(testSettings).
  settings(jsEnv := new SeleniumJSEnv(Firefox()))

lazy val seleniumJSHttpEnvTest: Project = project.
  enablePlugins(ScalaJSPlugin).
  enablePlugins(ScalaJSJUnitPlugin).
  settings(testSettings).
  settings(
    jsEnv := new SeleniumJSEnv(Firefox()).
      withMaterializer(new CustomFileMaterializer("tmp", "http://localhost:8080/tmp"))
  )
