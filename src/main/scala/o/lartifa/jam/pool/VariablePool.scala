package o.lartifa.jam.pool

import java.util.concurrent.Executors

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.exception.ExecuteException
import o.lartifa.jam.database.temporary.TemporaryMemory.database.db
import o.lartifa.jam.database.temporary.schema.Tables
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.model.CommandExecuteContext

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 变量池
 *
 * Author: sinar
 * 2020/1/3 23:58 
 */
object VariablePool {

  import o.lartifa.jam.database.temporary.TemporaryMemory.database.profile.api._

  private case class ChatInfo(chatType: String, chatId: Long)

  private implicit val exec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  private lazy val logger: HyLogger = JamContext.logger.get()

  /**
   * 更新变量
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 更新结果
   */
  def update(name: String, value: String)(implicit context: CommandExecuteContext): Future[String] = {
    val ChatInfo(chatType, chatId) = getChatInfo(context.eventMessage)
    logger.debug(s"变量更新：名称：$name，值：$value，聊天类型：$chatType，会话 ID：$chatId")
    val task = Variables
      .filter(row => row.chatId === chatId && row.chatType === chatType && row.name === name)
      .map(row => (row.name, row.value, row.chatType, row.chatId)) update ((name, value, chatType, chatId))
    db.run(task) map (count => if (count != 1) throw ExecuteException(s"变量更新失败！变量名为：$name") else value)
  }

  /**
   * 更新变量，若变量不存在，则将其设置默认值
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 执行结果
   */
  def updateOrElseDefault(name: String, value: String)(implicit context: CommandExecuteContext): Future[String] = {
    get(name).flatMap {
      case Some(_) => update(name, value)
      case None => add(name, value).map(_ => value)
    }
  }

  /**
   * 添加变量
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 添加结果
   */
  private def add(name: String, value: String)(implicit context: CommandExecuteContext): Future[Boolean] = {
    val ChatInfo(chatType, chatId) = getChatInfo(context.eventMessage)
    logger.debug(s"变量添加：名称：$name，值：$value，聊天类型：$chatType，会话 ID：$chatId")
    val task = Variables
      .map(row => (row.name, row.value, row.chatType, row.chatId)) += ((name, value, chatType, chatId))
    db.run(task) map (count => if (count != 1) throw ExecuteException(s"变量更新失败！变量名为：$name") else true)
  }

  /**
   * 获取变量
   *
   * @param name    变量名
   * @param context 执行上下文
   * @return 变量值（Optional）
   */
  def get(name: String)(implicit context: CommandExecuteContext): Future[Option[String]] = {
    val ChatInfo(chatType, chatId) = getChatInfo(context.eventMessage)
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
  def getOrElseUpdate(name: String, orElse: String)(implicit context: CommandExecuteContext): Future[String] = async {
    await(get(name)) match {
      case Some(value) => value
      case None => await(update(name, orElse))
    }
  }

  /**
   * 删除变量
   *
   * @param name    变量名
   * @param context 执行上下文
   * @return True：删除成功
   */
  def delete(name: String)(implicit context: CommandExecuteContext): Future[Boolean] = async {
    val ChatInfo(chatType, chatId) = getChatInfo(context.eventMessage)
    logger.debug(s"变量移除：名称：$name，聊天类型：$chatType，会话 ID：$chatId")
    await {
      db.run {
        Variables.filter(row => row.chatId === chatId && row.chatType === chatType && row.name === name).delete
      }
    }
    true
  }

  /**
   * 清除当前会话中的全部变量
   *
   * @param eventMessage 消息事件
   * @return True：删除成功
   */
  def cleanAllInChat(eventMessage: EventMessage): Future[Boolean] = async {
    val ChatInfo(chatType, chatId) = getChatInfo(eventMessage)
    await {
      db.run {
        Variables.filter(row => row.chatId === chatId && row.chatType === chatType).delete
      }
    }
    true
  }

  /**
   * 清除全部变量
   *
   * @return True：删除成功
   */
  def cleanAll(): Future[Boolean] = async {
    await(db.run(Variables.delete))
    true
  }

  /**
   * 清除全部变量
   *
   * @return 全部变量
   */
  def listAll(): Future[Seq[Tables.VariablesRow]] = async {
    await(db.run(Variables.result))
  }

  /**
   * 清除全部变量
   *
   * @param eventMessage 消息事件
   * @return 全部变量
   */
  def listAllInChart(eventMessage: EventMessage): Future[Seq[Tables.VariablesRow]] = async {
    val ChatInfo(chatType, chatId) = getChatInfo(eventMessage)
    await(db.run(Variables.filter(row => row.chatType === chatType && row.chatId === chatId).result))
  }

  /**
   * 获取聊天信息
   *
   * @param eventMessage 消息事件
   * @return 聊天信息对象
   */
  private def getChatInfo(eventMessage: EventMessage): ChatInfo = {
    eventMessage match {
      case message: EventGroupOrDiscussMessage =>
        ChatInfo(message.getMessageType, message.getGroup.getId)
      case message: EventPrivateMessage =>
        ChatInfo(message.getMessageType, message.getSenderId)
    }
  }
}
