lazy val `sbt-scalajs-play` = (project in file("."))
  .settings(
    sbtPlugin := true,
    name := "sbt-scalajs-play-routes",
    organization := "ch.timo_schmid",
    version := "0.0.1-SNAPSHOT",
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.15")
  )