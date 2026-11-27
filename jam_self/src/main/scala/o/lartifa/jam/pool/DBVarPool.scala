package o.lartifa.jam.pool

import o.lartifa.jam.onebot.domain.ChatMessage
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.common.util.TimeUtil
import o.lartifa.jam.database.Memory.database.db
import o.lartifa.jam.database.schema.Tables
import o.lartifa.jam.database.schema.Tables.*
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.pool.DBVarPool.logger

import scala.concurrent.{ExecutionContext, Future}

/**
 * 变量池
 *
 * Author: sinar
 * 2020/1/3 23:58
 */
class DBVarPool(implicit exec: ExecutionContext) extends VariablePool {

  import o.lartifa.jam.database.Memory.database.profile.api.*

  /**
   * 更新变量，不存在时报错
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 更新结果
   */
  override def update(name: String, value: String)(implicit context: CommandExecuteContext = null): Future[String] = {
    val ChatInfo(chatType, chatId) = if (context == null) ChatInfo.None else context.chatInfo
    logger.debug(s"变量更新：名称：$name，值：$value，聊天类型：$chatType，会话 ID：$chatId")
    val task = Variables
      .filter(row => row.chatId === chatId && row.chatType === chatType && row.name === name)
      .map(row => (row.name, row.value, row.chatType, row.chatId, row.lastUpdateDate))
      .update((name, value, chatType, chatId, TimeUtil.currentTimeStamp))
    db.run(task) map (count => if (count != 1) throw ExecutionException(s"变量更新失败，其可能不存在！变量名为：$name") else value)
  }

  /**
   * 更新变量，若变量不存在，则将其设置默认值
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 执行结果
   */
  override def updateOrElseSet(name: String, value: String)(implicit context: CommandExecuteContext = null): Future[String] = {
    get(name).flatMap {
      case Some(_) => update(name, value)
      case None => add(name, value).map(_ => value)
    }
  }

  /**
   * 更新变量，若变量不存在，则使用默认值（不更新）
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 执行结果
   */
  override def updateOrElseUse(name: String, value: String)(implicit context: CommandExecuteContext = null): Future[String] =
    DBVarPool.updateOrElseUse(get(name), update(name, value), value)(exec)

  /**
   * 添加变量
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 添加结果
   */
  private def add(name: String, value: String)(implicit context: CommandExecuteContext = null): Future[Boolean] = {
    val ChatInfo(chatType, chatId) = if (context == null) ChatInfo.None else context.chatInfo
    logger.debug(s"变量添加：名称：$name，值：$value，聊天类型：$chatType，会话 ID：$chatId")
    val task = Variables
      .map(row => (row.name, row.value, row.chatType, row.chatId, row.lastUpdateDate)) +=
      ((name, value, chatType, chatId, TimeUtil.currentTimeStamp))
    db.run(task) map (count => if (count != 1) throw ExecutionException(s"变量更新失败！变量名为：$name") else true)
  }

  /**
   * 获取变量
   *
   * @param name    变量名
   * @param context 执行上下文
   * @return 变量值（Optional）
   */
  override def get(name: String)(implicit context: CommandExecuteContext = null): Future[Option[String]] = {
    val ChatInfo(chatType, chatId) = if (context == null) ChatInfo.None else context.chatInfo
    db.run {
      Variables
        .filter(row => row.chatId === chatId && row.chatType === chatType && row.name === name)
        .map(row => row.value)
        .result
    } map (result => result.headOption)
  }

  /**
   * 取或更新变量
   *
   * @param name    变量名
   * @param orElse  若不存在，则用该值更新并返回
   * @param context 执行上下文
   * @return 变量值
   */
  override def getOrElseUpdate(name: String, orElse: String)(implicit context: CommandExecuteContext = null): Future[String] =
    get(name).flatMap {
      case Some(value) => Future.successful(value)
      case None => add(name, orElse).map(_ => orElse)
    }

  /**
   * 删除变量
   *
   * @param name    变量名
   * @param context 执行上下文
   * @return true：删除成功
   */
  override def delete(name: String)(implicit context: CommandExecuteContext = null): Future[Boolean] = {
    val ChatInfo(chatType, chatId) = if (context == null) ChatInfo.None else context.chatInfo
    logger.debug(s"变量移除：名称：$name，聊天类型：$chatType，会话 ID：$chatId")
    db.run {
      Variables.filter(row => row.chatId === chatId && row.chatType === chatType && row.name === name).delete
    }.map(_ => true)
  }

  /**
   * 清除当前会话中的全部变量
   *
   * @param eventMessage 消息事件
   * @return true：删除成功
   */
  override def cleanAllInChat(eventMessage: ChatMessage): Future[Boolean] = {
    val ChatInfo(chatType, chatId) = ChatInfo(eventMessage)
    db.run {
      Variables.filter(row => row.chatId === chatId && row.chatType === chatType).delete
    }.map(_ => true)
  }

  /**
   * 清除全部变量
   *
   * @return true：删除成功
   */
  override def cleanAll(): Future[Boolean] =
    db.run(Variables.delete).map(_ => true)

  /**
   * 列出全部变量
   *
   * @return 全部变量
   */
  override def listAll(): Future[Seq[Tables.VariablesRow]] =
    db.run(Variables.result)

  /**
   * 列出全部变量
   *
   * @param eventMessage 消息事件
   * @return 全部变量
   */
  override def listAllInChart(eventMessage: ChatMessage): Future[Seq[Tables.VariablesRow]] = {
    val ChatInfo(chatType, chatId) = ChatInfo(eventMessage)
    db.run(Variables.filter(row => row.chatType === chatType && row.chatId === chatId).result)
  }
}

object DBVarPool {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(DBVarPool.getClass)
  private implicit val executionContext: ExecutionContext = ThreadPools.DB

  def apply(): DBVarPool = new DBVarPool()

  /**
   * 变量存在时更新，不存在时直接使用默认值（纯 Future 逻辑，不依赖数据库）
   *
   * @param lookup  查询变量是否存在
   * @param doUpdate 变量存在时执行的更新
   * @param default 变量不存在时的默认值
   */
  private[jam] def updateOrElseUse(lookup: => Future[Option[String]], doUpdate: => Future[String], default: String)
                                  (exec: ExecutionContext): Future[String] =
    lookup.flatMap {
      case Some(_) => doUpdate
      case None => Future.successful(default)
    }(exec)
}
