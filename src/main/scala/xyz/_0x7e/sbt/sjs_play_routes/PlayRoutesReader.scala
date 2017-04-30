package xyz._0x7e.sbt.sjs_play_routes

import sbt._

import scala.io.Source

class PlayRoutesReader(log: Logger) {

  private val RE_LINE = "^(GET|POST|PUT|OPTIONS|DELETE|PATCH|->)\\s+([^\\s]+)\\s+(.*)$".r

  type ControllerContents = Map[String, Seq[RouteMethod]]

  type PackageContents = Map[String, ControllerContents]

  private [sjs_play_routes] def makeRoutes(routesFile: File): Map[String, PackageContents] = {
    val allRoutes = groupRoutes(getRoutesEntries(routesFile))
    val routes = allRoutes.values.map(_.values.map(_.values.map(_.size).sum).sum).sum
    val actions = allRoutes.values.map(_.values.map(_.size).sum).sum
    val controllers = allRoutes.mapValues(_.size).values.sum
    val packages = allRoutes.size
    log.info(s"Found $actions actions in $controllers controllers in $packages packages with $routes routes")
    allRoutes
  }

  private def groupRoutes(routeEntries: Seq[RoutesEntry]): Map[String, PackageContents] =
    routeEntries
      .flatMap(byPackage)
      .groupBy(_._1)
      .mapValues(_.map(_._2))
      .mapValues(toPackageContents)

  private def byPackage(entry: RoutesEntry): Option[(String, RoutesEntry)] =
    entry.actionControllerPackage match {
      case Some(pkg) => Some((pkg, entry))
      case None =>
        log.error(s"Skipping entry - no package found: $entry")
        None
    }

  private def toPackageContents(entries: Seq[RoutesEntry]): PackageContents =
    entries
      .flatMap(byControllerName)
      .groupBy(_._1)
      .mapValues(_.map(_._2))
      .mapValues(toControllerContents)

  private def byControllerName(entry: RoutesEntry): Option[(String, RoutesEntry)] =
    entry.actionControllerName match {
      case Some(ctrl) =>
        Some(ctrl, entry)
      case None =>
        log.error(s"Skipping entry - no controller name found: $entry")
        None
    }

  private def toControllerContents(entries: Seq[RoutesEntry]): ControllerContents =
    entries
      .flatMap(byActionName)
      .groupBy(_._1)
      .mapValues(_.map(_._2))

  private def byActionName(entry: RoutesEntry): Option[(String, RouteMethod)] =
    entry.actionRouteMethod match {
      case Some(method) => Some((method.name, method))
      case None =>
        log.error(s"Skipping entry - no action name found: $entry")
        None
    }

  private def getRoutesEntries(file: File): Seq[RoutesEntry] = {
    log.info(s"Reading file ${file.getAbsolutePath}")
    Source
      .fromFile(file)
      .getLines
      .toSeq
      .flatMap(toRoute)
  }

  private def toRoute(line: String): Option[RoutesEntry] =
    if(line.trim.isEmpty)
      None
    else if(line.trim.startsWith("#"))
      None
    else
      parseLine(line)

  private def parseLine(line: String): Option[RoutesEntry] =
    line match {
      case RE_LINE(method, pathSegment, action) =>
        Some(RoutesEntry(method, pathSegment, action))
      case _ =>
        log.warn(s"Line did not match: $line")
        None
    }

}
