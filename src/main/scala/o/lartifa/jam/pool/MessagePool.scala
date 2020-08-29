package o.lartifa.jam.pool

import java.util.concurrent.Executors

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.database.temporary.TemporaryMemory.database.db
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 全局消息词 / 短期记忆
 *
 * Author: sinar
 * 2020/1/13 22:11
 */
class MessagePool {

  import o.lartifa.jam.database.temporary.TemporaryMemory.database.profile.api._

  private implicit val exec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newWorkStealingPool(
    Runtime.getRuntime.availableProcessors() * 2
  ))

  private lazy val logger: HyLogger = JamContext.logger.get()

  /**
   * 记录聊天消息
   *
   * @param message 消息对象
   * @tparam T 消息类型
   * @return 变更结果（true：成功，false：失败）
   */
  def recordMessage[T <: EventMessage](message: T): Future[Boolean] = async {
    val result: Future[Int] = message match {
      case message: EventPrivateMessage => recordPrivateMessage(message)
      case message: EventGroupOrDiscussMessage => recordGroupOrDiscussMessage(message)
    }
    await(result) == 1
  }

  /**
   * 获取倒数第一条消息
   *
   * @param context 执行上下文
   * @return 消息记录（可能不存在）
   */
  def last(implicit context: CommandExecuteContext): Future[Option[MessageRecord]] = this.last(1)

  /**
   * 获取倒数第一条消息
   *
   * @param event 当前消息对象
   * @return 消息记录（可能不存在）
   */
  def last(event: EventMessage): Future[Option[MessageRecord]] = this.last(1, event)

  /**
   * 获取倒数第 n 条消息
   *
   * @param number  倒数第 n
   * @param context 执行上下文
   * @return 消息记录（可能不存在）
   */
  @throws[ExecutionException]
  def last(number: Int)(implicit context: CommandExecuteContext): Future[Option[MessageRecord]] =
    this.lasts(1, number)

  /**
   * 获取倒数第 n 条消息
   *
   * @param number 倒数第 n
   * @param event  当前消息对象
   * @return 消息记录（可能不存在）
   */
  @throws[ExecutionException]
  def last(number: Int, event: EventMessage): Future[Option[MessageRecord]] = this.lasts(1, number, event)

  /**
   * 获取倒数 n 条消息
   *
   * @param from    从倒数第 n 开始
   * @param take    取 n 条
   * @param context 执行上下文
   * @return 消息记录（可能不存在）
   */
  @throws[ExecutionException]
  def lasts(take: Int, from: Int = 1)(implicit context: CommandExecuteContext): Future[Option[MessageRecord]] =
    this.lasts(take, from, context.chatInfo)

  /**
   * 获取倒数 n 条消息
   *
   * @param from  从倒数第 n 开始
   * @param take  取 n 条
   * @param event 当前消息对象
   * @return 消息记录（可能不存在）
   */
  @throws[ExecutionException]
  def lasts(take: Int, from: Int = 1, event: EventMessage): Future[Option[MessageRecord]] = {
    this.lasts(take, from, ChatInfo(event))
  }

  /**
   * 获取倒数 n 条消息
   *
   * @param from     从倒数第 n 开始
   * @param take     取 n 条
   * @param chatInfo 会话状态
   * @return 消息记录（可能不存在）
   */
  @throws[ExecutionException]
  private def lasts(take: Int, from: Int = 1, chatInfo: ChatInfo): Future[Option[MessageRecord]] = {
    if (from <= 0) throw ExecutionException("倒数条数必须大于零")
    if (take <= 0) throw ExecutionException("获取消息数量必须大于零")
    val ChatInfo(chatType, chatId) = chatInfo
    db.run {
      (if (MessageType.PRIVATE == chatType) {
        MessageRecords.filter(row => row.messageType === chatType && row.senderId === chatId)
      } else {
        MessageRecords.filter(row => row.messageType === chatType && row.groupId === chatId)
      }).sortBy(_.timestamp.desc).drop(from - 1).take(take).result
    }.map(_.headOption)
  }

  /**
   * 是否当前处于复读状态
   *
   * @param context 执行上下文
   * @return 是否复读
   */
  def isRepeat(implicit context: CommandExecuteContext): Future[Boolean] = {
    this.last.map(_.exists(_.message == context.eventMessage.getMessage))
  }

  /**
   * 是否当前处于复读状态
   *
   * @param event 当前消息对象
   * @return 是否复读
   */
  def isRepeat(event: EventMessage): Future[Boolean] = {
    this.last(event).map(_.exists(_.message == event.getMessage))
  }

  /**
   * 记录私聊消息
   *
   * @param msg 消息对象
   * @return 变更结果
   */
  private def recordPrivateMessage(msg: EventPrivateMessage): Future[Int] = {
    db.run {
      MessageRecords
        .map(row =>
          (row.message, row.messageId, row.messageType, row.messageSubType, row.postType,
            row.rawMessage, row.selfId, row.senderId, row.font, row.timestamp)
        ) += (msg.getMessage, msg.getMessageId, msg.getMessageType, msg.getSubType, msg.getPostType,
        msg.getRawMessage, msg.getSelfId, msg.getSenderId, msg.getFont, msg.getTime)
    }
  }

  /**
   * 记录群聊 / 讨论组消息
   *
   * @param msg 消息对象
   * @return 变更结果
   */
  private def recordGroupOrDiscussMessage(msg: EventGroupOrDiscussMessage): Future[Int] = {
    db.run {
      MessageRecords
        .map(row =>
          (row.message, row.messageId, row.messageType, row.messageSubType, row.postType,
            row.rawMessage, row.selfId, row.senderId, row.groupId, row.font, row.timestamp)
        ) += (msg.getMessage, msg.getMessageId, msg.getMessageType, MessageType.SUB_TYPE_NORMAL, msg.getPostType,
        msg.getRawMessage, msg.getSelfId, msg.getSenderId, msg.getGroup.getId, msg.getFont, msg.getTime)
    }
  }
}
