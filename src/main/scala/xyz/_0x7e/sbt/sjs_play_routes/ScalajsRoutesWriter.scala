package xyz._0x7e.sbt.sjs_play_routes

import sbt.{Logger, _}

/**
  * Writes the routes to a directory
  */
class ScalajsRoutesWriter(targetDir: File, prefix: String, log: Logger) {

  // TODO cleanup dupes
  type ControllerContents = Map[String, Seq[RouteMethod]]

  type PackageContents = Map[String, ControllerContents]

  private lazy val prefixSafe = if(prefix.isEmpty) "" else if(prefix.endsWith(".")) prefix else s"$prefix."

  def writeRoutes(routes: Map[String, PackageContents]): Seq[File] =
    routes
      .toSeq
      .flatMap(pkg => writePackage(pkg._1, pkg._2))

  private def writePackage(packageName: String, content: PackageContents): Seq[File] = {
    log.info(s"Writing package... $packageName")
    content
      .toSeq
      .flatMap(ctrl => writeController(packageName, ctrl._1, ctrl._2))
  }

  private def writeController(packageName: String, controllerName: String, content: ControllerContents): Seq[File] = {
    log.info(s"Writing controller... $packageName.$controllerName")
    val file = new File(controllerFile(packageName, controllerName))
    log.debug(s"Writing to file: ${file.getAbsolutePath}")
    val fileContent =
      s"""package $prefixSafe$packageName
         |
         |import fr.hmil.roshttp.HttpRequest
         |import fr.hmil.roshttp.response.SimpleHttpResponse
         |import fr.hmil.roshttp.Method._
         |import monix.execution.Scheduler.Implicits.global
         |import scala.concurrent.Future
         |
         |/**
         | * Automatically generated
         | * Routes for $controllerName
         | */
         |object $controllerName {
         |
         |  type Call = Future[SimpleHttpResponse]
         |
         |  private def addUrlParam(request: HttpRequest, param: (String, String)): HttpRequest =
         |    request.withQueryParameter(param._1, param._2)
         |
         |  private def foldParam(acc: (String, Map[String, String]), param: (String, String)): (String, Map[String, String]) =
         |    if(acc._1.contains(s":$${param._1}"))
         |      (acc._1.replaceAll(s":$${param._1}", s"$${param._2}"), acc._2)
         |    else
         |      (acc._1, acc._2 + param)
         |
         |${routeMethods(content)}
         |
         |}
       """.stripMargin
    println(fileContent)
    IO.write(file, fileContent)
    Seq(file)
  }

  private def routeMethods(content: ControllerContents): String =
    content
      .flatMap(route => routeMethod(route._1, route._2))
      .mkString("\n\n")

  private def routeMethod(methodName: String, overloads: Seq[RouteMethod]): Seq[String] =
    Seq(
      s"""  def $methodName(${methodParams(overloads)})(implicit httpRequest: HttpRequest): Call = {
         |    (${paramsForMatch(overloads)}) match {
         |${casesForMatch(overloads)}
         |    }
         |  }
       """.stripMargin)

  private def methodParams(overloads: Seq[RouteMethod]): String =
    if(overloads.isEmpty)
      ""
    else
      overloads
        .head
        .params
        .map { param =>
          s"`${param.name}`: ${param.tpe}${paramDefault(param)}"
        }
        .mkString(", ")

  private def paramsForMatch(overloads: Seq[RouteMethod]): String =
    if(overloads.isEmpty)
      ""
    else
      overloads
        .head
        .params
        .map(param => s"`${param.name}`")
        .mkString(", ")

  private def casesForMatch(overloads: Seq[RouteMethod]): String =
    overloads.map { overload =>
      s"""      case (${paramsForCase(overload)}) ${ifForCase(overload)} =>
         |${routeMethod(overload)}
         |""".stripMargin
    }.mkString("\n")

  private def paramsForCase(overload: RouteMethod): String =
    overload
      .params
      .map(toParamForCase)
      .mkString(", ")

  private def toParamForCase(param: RouterParam): String =
    s"`${param.name}`"

  private def ifForCase(overload: RouteMethod): String = {
    val ifConditions = overload.params.flatMap(toIfCondition)
    if(ifConditions.isEmpty)
      ""
    else
      ifConditions.mkString("if ", " && ", "")
  }

  private def toIfCondition(param: RouterParam): Option[String] =
    param.value match {
      case DefaultParam(value) => None
      case FixedParam(value) => Some(s"${param.name} == $value")
      case _ => None
    }

  private def routeMethod(overload: RouteMethod): String =
    s"""          val params = ${routeParams(overload)}
       |          val urlAndParams: (String, Map[String, String]) =
       |            params
       |              .toSeq
       |              .sortBy(_._1.length)
       |              .reverse
       |              .foldLeft((s"${basicUrl(overload)}", Map[String, String]()))(foldParam)
       |
       |          ${toRequest(overload.method)} """.stripMargin

  private def toRequest(method: String): String =
    s"""   urlAndParams._2
       |              .foldLeft(httpRequest.withMethod(GET).withPath(urlAndParams._1))(addUrlParam)
       |              .send()
     """.stripMargin

  private def basicUrl(overload: RouteMethod): String =
    overload
      .pathSegment
      .replace("\\", "\\\\")

  private def routeParams(overload: RouteMethod): String =
    s""" Map[String, String](
       |${replaceParams(overload)}
       |          ) """.stripMargin

  private def replaceParams(overload: RouteMethod): String =
    overload
      .params
      .map(toReplaceParam)
      .mkString(",\n")

  private def toReplaceParam(param: RouterParam): String =
    s"""            "${param.name}" -> `${param.name}`.toString"""

  private def paramDefault(param: RouterParam): String =
    param.value match {
      case DefaultParam(value) => s" = $value"
      case FixedParam(value) => " /* FIXED */"
      case _ => ""
    }

  private def controllerFile(packageName: String, controllerName: String): String =
    targetDir / s"$prefixSafe$packageName.$controllerName".replaceAll("\\.", "/") + ".scala"

}

