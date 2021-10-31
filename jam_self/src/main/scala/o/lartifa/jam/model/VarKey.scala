package o.lartifa.jam.model

import o.lartifa.jam.model.VarKey.Category

import scala.concurrent.{ExecutionContext, Future}

/**
 * 变量键
 *
 * Author: sinar
 * 2020/7/18 00:21
 */
case class VarKey(name: String, category: Category, private var _defaultValue: Option[String] = None) {
  def defaultValue: Option[String] = _defaultValue

  /**
   * 更新变量
   *
   * @param value   变量值
   * @param context 执行上下文
   * @return 更新结果
   */
  def update(value: String)(implicit context: CommandExecuteContext): Future[String] = {
    category match {
      case VarKey.DB => context.vars.update(name, value)
      case VarKey.Temp => context.tempVars.update(name, value)
      case VarKey.Mocked =>
        this._defaultValue = Some(value)
        Future.successful(value)
    }
  }

  /**
   * 更新变量，若变量不存在，则将其设置默认值
   *
   * @param value   变量值
   * @param context 执行上下文
   * @return 执行结果
   */
  def updateOrElseSet(value: String)(implicit context: CommandExecuteContext): Future[String] = {
    category match {
      case VarKey.DB => context.vars.updateOrElseSet(name, value)
      case VarKey.Temp => context.tempVars.updateOrElseSet(name, value)
      case VarKey.Mocked =>
        this._defaultValue = Some(value)
        Future.successful(value)
    }
  }

  /**
   * 更新变量，若变量不存在，则使用默认值（不更新）
   *
   * @param value   变量值
   * @param context 执行上下文
   * @return 执行结果
   */
  def updateOrElseUse(value: String)(implicit context: CommandExecuteContext): Future[String] = {
    category match {
      case VarKey.DB => context.vars.updateOrElseUse(name, value)
      case VarKey.Temp => context.tempVars.updateOrElseUse(name, value)
      case VarKey.Mocked =>
        this._defaultValue = Some(value)
        Future.successful(value)
    }
  }

  /**
   * 获取变量
   *
   * @param context 执行上下文
   * @return 变量值（Optional）
   */
  def query(implicit context: CommandExecuteContext): Future[Option[String]] = {
    implicit val exec: ExecutionContext = context.executionContext
    category match {
      case VarKey.DB => context.vars.get(name).map(_.orElse(defaultValue))
      case VarKey.Temp => context.tempVars.get(name).map(_.orElse(defaultValue))
      case VarKey.Mocked => Future.successful(this._defaultValue)
    }
  }

  /**
   * 取或更新变量
   *
   * @param orElse  若不存在，则用该值更新并返回
   * @param context 执行上下文
   * @return 变量值
   */
  def queryOrElseUpdate(orElse: String)(implicit context: CommandExecuteContext): Future[String] = {
    category match {
      case VarKey.DB => context.vars.getOrElseUpdate(name, orElse)
      case VarKey.Temp => context.tempVars.getOrElseUpdate(name, orElse)
      case VarKey.Mocked =>
        this._defaultValue = Some(orElse)
        Future.successful(orElse)
    }
  }

  /**
   * 删除变量
   *
   * @param context 执行上下文
   * @return true：删除成功
   */
  def delete(implicit context: CommandExecuteContext): Future[Boolean] = {
    category match {
      case VarKey.DB => context.vars.delete(name)
      case VarKey.Temp => context.tempVars.delete(name)
      case VarKey.Mocked => Future.successful(true)
    }
  }
}

object VarKey {

  sealed abstract class Category(val name: String)

  case object DB extends Category("变量")

  case object Temp extends Category("临时变量")

  case object Mocked extends Category("模拟变量")

  object Type {
    val temp: Set[String] = Set("临时变量", "*变量", "*$")
    val db: Set[String] = Set("变量", "$")
    val templateVarDB: Set[String] = Set("@", "#", "&", "P", "图")
    val templateVarTemp: Set[String] = templateVarDB.map("*" + _)
  }

}
