addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.16")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.1.14")

addSbtPlugin("org.scalastyle" % "scalastyle-sbt-plugin" % "0.8.0")

libraryDependencies ++= Seq(
  "org.scala-js" %% "scalajs-js-envs" % "0.6.16",
  "org.scala-js" %% "scalajs-tools" % "0.6.16",
  "org.seleniumhq.selenium" % "selenium-java" % "3.4.0",
  "org.seleniumhq.selenium" % "selenium-chrome-driver" % "3.4.0",
  "org.seleniumhq.selenium" % "selenium-firefox-driver" % "3.4.0"
)

unmanagedSourceDirectories in Compile ++= {
  val root = baseDirectory.value.getParentFile
  Seq(root / "seleniumJSEnv/src/main/scala")
}
