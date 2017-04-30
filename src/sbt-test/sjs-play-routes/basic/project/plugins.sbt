sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("xyz.0x7e" % "sbt-scalajs-play-routes" % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}