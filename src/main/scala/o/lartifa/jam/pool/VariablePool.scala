package o.lartifa.jam.pool
import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.database.temporary.schema.Tables
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.Future

/**
 * Author: sinar
 * 2020/7/18 18:05
 */
trait VariablePool {

  /**
   * 更新变量，不存在时报错
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 更新结果
   */
  def update(name: String, value: String)(implicit context: CommandExecuteContext): Future[String]

  /**
   * 更新变量，若变量不存在，则将其设置默认值
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 执行结果
   */
  def updateOrElseSet(name: String, value: String)(implicit context: CommandExecuteContext): Future[String]

  /**
   * 更新变量，若变量不存在，则使用默认值（不更新）
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 执行结果
   */
  def updateOrElseUse(name: String, value: String)(implicit context: CommandExecuteContext): Future[String]

  /**
   * 获取变量
   *
   * @param name    变量名
   * @param context 执行上下文
   * @return 变量值（Optional）
   */
  def get(name: String)(implicit context: CommandExecuteContext): Future[Option[String]]

  /**
   * 取或更新变量
   *
   * @param name    变量名
   * @param orElse  若不存在，则用该值更新并返回
   * @param context 执行上下文
   * @return 变量值
   */
  def getOrElseUpdate(name: String, orElse: String)(implicit context: CommandExecuteContext): Future[String]

  /**
   * 删除变量
   *
   * @param name    变量名
   * @param context 执行上下文
   * @return true：删除成功
   */
  def delete(name: String)(implicit context: CommandExecuteContext): Future[Boolean]

  /**
   * 清除当前会话中的全部变量
   *
   * @param eventMessage 消息事件
   * @return true：删除成功
   */
  def cleanAllInChat(eventMessage: EventMessage): Future[Boolean]

  /**
   * 清除全部变量
   *
   * @return true：删除成功
   */
  def cleanAll(): Future[Boolean]

  /**
   * 清除全部变量
   *
   * @return 全部变量
   */
  def listAll(): Future[Seq[Tables.VariablesRow]]

  /**
   * 清除全部变量
   *
   * @param eventMessage 消息事件
   * @return 全部变量
   */
  def listAllInChart(eventMessage: EventMessage): Future[Seq[Tables.VariablesRow]]
}
