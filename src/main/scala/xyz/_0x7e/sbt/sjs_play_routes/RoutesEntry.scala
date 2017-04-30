package xyz._0x7e.sbt.sjs_play_routes

final case class RoutesEntry(method: String, pathSegment: String, action: String) {

  private val RE_ACTION_WITHOUT_PARAMS = "^([^(]*).*".r

  private val RE_LAST_SEGMENT = "^.*[.]([^.]+)$".r

  private val RE_PARAMS = "^[^(]*[(](.*)[)]$".r

  private val RE_WITHOUT_LAST_SEGMENT = "^(.*)[.]([^.]*)$".r

  private val RE_PARAM_NAME_COLON = "^([^:)]+)\\:.*$".r

  private val RE_PARAM_NAME_QMARK = "^([^?)]+)\\?=.*$".r

  private val RE_PARAM_NAME_EQUAL = "^([^=)]+)\\=.*$".r

  private val RE_PARAM_NAME_PLAIN = "^([^=^?^:)]+)$".r

  private val RE_PARAM_TYPE = "^([^:^)]*):([^?^=]*).*".r

  private val RE_PARAM_VALUE_FIXED = "^[^=]*=(.*)".r

  private val RE_PARAM_VALUE_DEFAUlT = "^[^=]*\\?=(.*)".r

  def actionWithoutParams: Option[String] =
    action match {
      case RE_ACTION_WITHOUT_PARAMS(ac) => Some(ac)
      case _ => None
    }

  def actionMethodName: Option[String] =
    actionWithoutParams.flatMap { ac =>
      ac match {
        case RE_LAST_SEGMENT(methodName) => Some(methodName)
        case _ => None
      }
    }

  def actionController: Option[String] =
    actionWithoutParams.flatMap { ac =>
      ac match {
        case RE_WITHOUT_LAST_SEGMENT(controller, _) => Some(controller)
        case _ => None
      }
    }

  def actionControllerName: Option[String] = {
    actionController.flatMap { controller =>
      controller match {
        case RE_LAST_SEGMENT(controllerName) => Some(controllerName)
        case _ => None
      }
    }
  }

  def actionControllerPackage: Option[String] =
    actionController.flatMap { controllerName =>
      controllerName match {
        case RE_WITHOUT_LAST_SEGMENT(pkg, _) => Some(pkg)
        case _ => None
      }
    }

  def actionParamString: Option[String] =
    action match {
      case RE_PARAMS(params) if params.trim.nonEmpty && params != "()" => Some(params)
      case _ => None
    }

  def actionRouteMethod: Option[RouteMethod] =
    actionMethodName.map { methodName =>
      RouteMethod(method, methodName, actionParams, pathSegment)
    }

  def actionParams: Seq[RouterParam] =
    actionParamString match {
      case Some(paramString) =>
        paramString.split(",").toSeq.flatMap(toParam)
      case None => Seq()
    }

  private def toParam(param: String): Option[RouterParam] =
    paramName(param) match {
      case Some(name) =>
        Some(RouterParam(name, paramType(param).getOrElse("Any"), paramValue(param)))
      case None =>
        println(s"Could not parse parameter: $param")
        None
    }

  private def paramValue(param: String): ParamValue = {
    param.trim match {
      case RE_PARAM_VALUE_DEFAUlT(value) => DefaultParam(value.trim)
      case RE_PARAM_VALUE_FIXED(value) => FixedParam(value.trim)
      case _ => NoValue
    }
  }

  private def paramName(param: String): Option[String] =
    param.trim match {
      case RE_PARAM_NAME_COLON(name) => Some(name.trim)
      case RE_PARAM_NAME_QMARK(name) => Some(name.trim)
      case RE_PARAM_NAME_EQUAL(name) => Some(name.trim)
      case RE_PARAM_NAME_PLAIN(name) => Some(name.trim)
      case _ => None
    }

  private def paramType(param: String): Option[String] =
    param match {
      case RE_PARAM_TYPE(_, tpe) => Some(tpe.trim)
      case _ => None
    }

}
