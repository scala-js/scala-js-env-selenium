addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.8")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.1.8")

addSbtPlugin("org.scalastyle" % "scalastyle-sbt-plugin" % "0.8.0")

libraryDependencies += ("org.scala-js" %% "scalajs-js-envs" % "0.6.7")

libraryDependencies += ("org.seleniumhq.selenium" % "selenium-java" % "2.53.0")
libraryDependencies += ("org.seleniumhq.selenium" % "selenium-chrome-driver" % "2.53.0")

unmanagedSourceDirectories in Compile ++= {
  val root = baseDirectory.value.getParentFile
  Seq(root / "seleniumJSEnv/src/main/scala")
}
