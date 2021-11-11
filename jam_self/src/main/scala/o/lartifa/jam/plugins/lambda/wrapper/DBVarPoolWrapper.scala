package o.lartifa.jam.plugins.lambda.wrapper

import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.database.schema.Tables
import o.lartifa.jam.pool.DBVarPool

import java.util.Optional
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.jdk.CollectionConverters.*

/**
 * 数据库变量池包装器
 *
 * Author: sinar
 * 2021/11/11 23:01
 */
class DBVarPoolWrapper(pool: DBVarPool, eventMessage: EventMessage)(implicit exec: ExecutionContext) {
  /**
   * 更新变量，不存在时报错
   *
   * @param name  变量名
   * @param value 变量值
   * @return 更新结果
   */
  def update(name: String, value: String): String = Await.result(pool.update(name, value), Duration.Inf)

  /**
   * 更新变量，若变量不存在，则将其设置默认值
   *
   * @param name  变量名
   * @param value 变量值
   * @return 执行结果
   */
  def updateOrElseSet(name: String, value: String): String = Await.result(pool.updateOrElseSet(name, value), Duration.Inf)

  /**
   * 更新变量，若变量不存在，则使用默认值（不更新）
   *
   * @param name  变量名
   * @param value 变量值
   * @return 执行结果
   */
  def updateOrElseUse(name: String, value: String): String = Await.result(pool.updateOrElseUse(name, value), Duration.Inf)

  /**
   * 获取变量
   *
   * @param name 变量名
   * @return 变量值（Optional）
   */
  def get(name: String): Optional[String] = Optional.ofNullable(Await.result(pool.get(name), Duration.Inf).orNull)

  /**
   * 取或更新变量
   *
   * @param name   变量名
   * @param orElse 若不存在，则用该值更新并返回
   * @return 变量值
   */
  def getOrElseUpdate(name: String, orElse: String): String = Await.result(pool.getOrElseUpdate(name, orElse), Duration.Inf)

  /**
   * 删除变量
   *
   * @param name 变量名
   * @return true：删除成功
   */
  def delete(name: String): Boolean = Await.result(pool.delete(name), Duration.Inf)

  /**
   * 清除当前会话中的全部变量
   *
   * @return true：删除成功
   */
  def cleanAllInChat(): Boolean = Await.result(pool.cleanAllInChat(eventMessage), Duration.Inf)

  /**
   * 清除全部变量（不支持，该方法极易误操作，所以不暴露给 Lambda，仅在编写插件时可用）
   *
   * def cleanAll(): Boolean
   */

  /**
   * 列出全部变量
   *
   * @return 全部变量
   */
  def listAll(): java.util.List[Tables.VariablesRow] = Await.result(pool.listAll(), Duration.Inf).asJava

  /**
   * 获取当前会话中全部变量
   *
   * @return 全部变量
   */
  def listAllInChart(): java.util.List[Tables.VariablesRow] = Await.result(pool.listAllInChart(eventMessage), Duration.Inf).asJava
}
