package o.lartifa.jam.pool

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.database.Memory.database.db
import o.lartifa.jam.database.schema.Tables.*
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.plugins.filppic.QQImg

import java.util.UUID
import scala.async.Async.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * 全局消息词 / 短期记忆
 *
 * Author: sinar
 * 2020/1/13 22:11
 */
class MessagePool {

  import o.lartifa.jam.database.Memory.database.profile.api.*

  private implicit val exec: ExecutionContext = ThreadPools.DB

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
   * 记录占位符，代表果酱处理过某条消息
   *
   * @param message 消息对象
   * @param content 消息类型
   * @tparam T 消息类型
   * @return 变更结果（true：成功，false：失败）
   */
  def recordAPlaceholder[T <: EventMessage](message: T, content: String): Future[Boolean] = async {
    val result: Future[Int] = message match {
      case message: EventPrivateMessage => recordPrivatePlaceholder(message, content)
      case message: EventGroupOrDiscussMessage => recordGroupOrDiscussPlaceholder(message, content)
    }
    await(result) == 1
  }

  /**
   * 获取前一条（倒数第二条消息）
   *
   * @param context 执行上下文
   * @return 消息记录（可能不存在）
   */
  def previousMessage(implicit context: CommandExecuteContext): Future[Option[MessageRecord]] = this.last(2)

  /**
   * 获取前一条（倒数第二条消息）
   *
   * @param event 当前消息对象
   * @return 消息记录（可能不存在）
   */
  def previousMessage(event: EventMessage): Future[Option[MessageRecord]] = this.last(event, 2)

  /**
   * 获取倒数第 n 条消息
   *
   * @param number  倒数第 n
   * @param context 执行上下文
   * @return 消息记录（可能不存在）
   */
  @throws[ExecutionException]
  def last(number: Int)(implicit context: CommandExecuteContext): Future[Option[MessageRecord]] =
    this.lasts(1, number).map(_.headOption)

  /**
   * 获取倒数第 n 条消息
   *
   * @param number 倒数第 n
   * @param event  当前消息对象
   * @return 消息记录（可能不存在）
   */
  @throws[ExecutionException]
  def last(event: EventMessage, number: Int): Future[Option[MessageRecord]] =
    this.lasts(event, 1, number).map(_.headOption)

  /**
   * 获取倒数 n 条消息
   *
   * @param take    取 n 条
   * @param from    从倒数第 n 开始
   * @param context 执行上下文
   * @return 消息记录（可能不存在）
   */
  @throws[ExecutionException]
  def lasts(take: Int, from: Int)(implicit context: CommandExecuteContext): Future[Seq[MessageRecord]] =
    this.lastMessages(take, from, context.chatInfo)

  /**
   * 获取倒数 n 条消息
   *
   * @param event 当前消息对象
   * @param take  取 n 条
   * @param from  从倒数第 n 开始
   * @return 消息记录（可能不存在）
   */
  @throws[ExecutionException]
  def lasts(event: EventMessage, take: Int, from: Int): Future[Seq[MessageRecord]] =
    this.lastMessages(take, from, ChatInfo(event))

  /**
   * 获取倒数 n 条消息
   *
   * @param from     从倒数第 n 开始
   * @param take     取 n 条
   * @param chatInfo 会话状态
   * @return 消息记录（可能不存在）
   */
  @throws[ExecutionException]
  private def lastMessages(take: Int, from: Int, chatInfo: ChatInfo): Future[Seq[MessageRecord]] = {
    if (from < 1) throw ExecutionException("倒数条数必须大于或等于1")
    if (take <= 0) throw ExecutionException("获取消息数量必须大于或等于零")
    val ChatInfo(chatType, chatId) = chatInfo
    db.run {
      (if (MessageType.PRIVATE == chatType) {
        MessageRecords.filter(row => row.messageType === chatType && row.senderId === chatId)
      } else {
        MessageRecords.filter(row => row.messageType === chatType && row.groupId === chatId)
      }).sortBy(_.id.desc).drop(from - 1).take(take).result
    }
  }

  /**
   * 是否当前处于复读状态
   *
   * @param context 执行上下文
   * @return 是否复读
   */
  def isRepeat(implicit context: CommandExecuteContext): Future[Boolean] = this.isRepeat(context.eventMessage)

  /**
   * 是否复读一张图片
   *
   * @param context 执行上下文
   * @return 是否复读
   */
  def isPictureRepeat(implicit context: CommandExecuteContext): Future[Boolean] = this.isPictureRepeat(context.eventMessage)

  /**
   * 是否当前处于复读状态
   *
   * @param event 当前消息对象
   * @return 是否复读
   */
  def isRepeat(event: EventMessage): Future[Boolean] = {
    this.lasts(event, 2, 1).map { it =>
      it.sizeIs == 2 && it.head.message == it.last.message
    }
  }

  /**
   * 是否复读一张图片
   *
   * @param event 当前消息对象
   * @return 是否复读
   */
  def isPictureRepeat(event: EventMessage): Future[Boolean] = {
    this.lasts(event, 2, 1).map { it =>
      it.sizeIs == 2 && QQImg.isPicSame(it.head.message, it.last.message)
    }
  }

  /**
   * 清理超过 N 天的消息记录
   *
   * @param days 天数
   * @return 删除的消息数量
   */
  def cleanUpMessage(days: Int): Future[Int] = {
    db.run(sqlu"""delete from message_records where timestamp <= date_trunc('day', (current_timestamp - interval '#$days' day));""")
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
            row.rawMessage, row.selfId, row.senderId, row.font)
        ) += (msg.getMessage, msg.getMessageId, msg.getMessageType, msg.getSubType, msg.getPostType,
        msg.getRawMessage, msg.getSelfId, msg.getSenderId, msg.getFont)
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
            row.rawMessage, row.selfId, row.senderId, row.groupId, row.font)
        ) += (msg.getMessage, msg.getMessageId, msg.getMessageType, MessageType.SUB_TYPE_NORMAL, msg.getPostType,
        msg.getRawMessage, msg.getSelfId, msg.getSenderId, msg.getGroup.getId, msg.getFont)
    }
  }

  /**
   * 记录私聊占位符
   *
   * @param msg     消息对象
   * @param content 消息类型
   * @return 变更结果
   */
  private def recordPrivatePlaceholder(msg: EventPrivateMessage, content: String): Future[Int] = {
    val placeholderMessage = s"$content ${UUID.randomUUID().toString}"
    db.run {
      MessageRecords
        .map(row =>
          (row.message, row.messageId, row.messageType, row.messageSubType, row.postType,
            row.rawMessage, row.selfId, row.senderId, row.font)
        ) += (placeholderMessage, -1, msg.getMessageType, msg.getSubType, "placeholder",
        placeholderMessage, msg.getSelfId, msg.getSelfId, msg.getFont)
    }
  }

  /**
   * 记录群聊 / 讨论组占位符
   *
   * @param msg     消息对象
   * @param content 消息类型
   * @return 变更结果
   */
  private def recordGroupOrDiscussPlaceholder(msg: EventGroupOrDiscussMessage, content: String): Future[Int] = {
    val placeholderMessage = s"$content ${UUID.randomUUID().toString}"
    db.run {
      MessageRecords
        .map(row =>
          (row.message, row.messageId, row.messageType, row.messageSubType, row.postType,
            row.rawMessage, row.selfId, row.senderId, row.groupId, row.font)
        ) += (placeholderMessage, -1, msg.getMessageType, MessageType.SUB_TYPE_NORMAL, "placeholder",
        placeholderMessage, msg.getSelfId, msg.getSelfId, msg.getGroup.getId, msg.getFont)
    }
  }
}
