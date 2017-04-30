package xyz._0x7e.sbt.sjs_play_routes

sealed trait ParamValue {
  def isDefault: Boolean
  def isFixed: Boolean
}

case object NoValue extends ParamValue {
  override def isDefault = false
  override def isFixed = false

}

final case class DefaultParam(value: String) extends ParamValue {
  override def isDefault = true
  override def isFixed = false
}

final case class FixedParam(value: String) extends ParamValue {
  override def isDefault = false
  override def isFixed = true
}
