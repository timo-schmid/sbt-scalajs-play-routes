package ch.timo_schmid.sbt.scalajs_play_routes

final case class RouteMethod(method: String, name: String, params: Seq[RouterParam], pathSegment: String)
