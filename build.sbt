lazy val `sbt-scalajs-play` = (project in file("."))
  .settings(
    sbtPlugin := true,
    name := "sbt-sjs-play-routes",
    organization := "xyz.0x7e",
    version := "0.0.1-SNAPSHOT",
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.15")
  )