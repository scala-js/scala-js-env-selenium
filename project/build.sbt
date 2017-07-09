addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.18")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.1.8")

addSbtPlugin("org.scalastyle" % "scalastyle-sbt-plugin" % "0.8.0")

libraryDependencies += ("org.seleniumhq.selenium" % "selenium-server" % "3.4.0")

unmanagedSourceDirectories in Compile ++= {
  val root = baseDirectory.value.getParentFile
  Seq(root / "seleniumJSEnv/src/main/scala")
}
