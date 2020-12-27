package o.lartifa.jam.model

import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.VarKey.Category

import scala.concurrent.Future

/**
 * 变量键
 *
 * Author: sinar
 * 2020/7/18 00:21
 */
case class VarKey(name: String, category: Category) {

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
      case VarKey.Template => Future.failed(ExecutionException("模板变量不应该被更新"))
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
      case VarKey.Template => Future.failed(ExecutionException("模板变量不应该被更新"))
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
      case VarKey.Template => Future.failed(ExecutionException("模板变量不应该被更新"))
    }
  }

  /**
   * 获取变量
   *
   * @param context 执行上下文
   * @return 变量值（Optional）
   */
  def query(implicit context: CommandExecuteContext): Future[Option[String]] = {
    category match {
      case VarKey.DB => context.vars.get(name)
      case VarKey.Temp => context.tempVars.get(name)
      case VarKey.Template => Future.failed(ExecutionException("当前变量不是模板变量"))
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
      case VarKey.Template => Future.failed(ExecutionException("模板变量不应该被更新"))
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
      case VarKey.Template => Future.failed(ExecutionException("模板变量不应该被删除"))
    }
  }
}

object VarKey {

  sealed abstract class Category(val name: String)

  case object DB extends Category("变量")

  case object Temp extends Category("临时变量")

  case object Template extends Category("模板变量")

  object Type {
    val temp: Set[String] = Set("临时变量", "*变量", "*$")
    val db: Set[String] = Set("变量", "$")
    val templateVarDB: Set[String] = Set("@", "#", "&", "P", "图")
    val templateVarTemp: Set[String] = templateVarDB.map("*" + _)
  }

}
