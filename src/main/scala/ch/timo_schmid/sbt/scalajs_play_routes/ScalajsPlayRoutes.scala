package ch.timo_schmid.sbt.scalajs_play_routes

import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt.Def.Setting
import sbt.Keys.{libraryDependencies, sourceGenerators, sourceManaged, streams}
import sbt._

/**
 * Generates the routes for the client side of the project
 */
object ScalajsPlayRoutes extends AutoPlugin {

  object autoImport {

    lazy val scalajsPlayRoutes: TaskKey[Seq[File]] =
      taskKey[Seq[File]]("Generates play routes for the scalajs project")

    lazy val scalajsPlayRoutesFile: SettingKey[String] =
      settingKey[String]("The path to the play routes file")

    lazy val scalajsPlayRoutesPrefix: SettingKey[String] =
      settingKey[String]("The prefix for play routes")

  }

  import ScalaJSPlugin.autoImport._
  import autoImport._

  override def requires: Plugins = ScalaJSPlugin

  override def projectSettings: Seq[Setting[_]] = Seq(
    scalajsPlayRoutesPrefix := "routes",
    scalajsPlayRoutes := {
      val log = streams.value.log
      new ScalajsRoutesWriter(sourceManaged.value, scalajsPlayRoutesPrefix.value, log).writeRoutes(
        new PlayRoutesReader(log).makeRoutes(new File(scalajsPlayRoutesFile.value))
      )
    },
    sourceGenerators in Compile += scalajsPlayRoutes.taskValue,
    libraryDependencies += "fr.hmil" %%% "roshttp" % "2.0.1"
  )

}

