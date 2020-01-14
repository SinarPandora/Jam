package o.lartifa.jam.pool

import java.util.concurrent.Executors

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.database.temporary.TemporaryMemory.database.db
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.pool.MessagePool.Constant

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

  private implicit val exec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

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
        msg.getRawMessage, msg.getSelfId, msg.getSelfId, msg.getFont, msg.getTime)
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
        ) += (msg.getMessage, msg.getMessageId, msg.getMessageType, Constant.SUB_TYPE_NORMAL, msg.getPostType,
        msg.getRawMessage, msg.getSelfId, msg.getSelfId, msg.getGroup.getId, msg.getFont, msg.getTime)
    }
  }
}

object MessagePool {
  object Constant {
    val SUB_TYPE_NORMAL: String = "normal"
    val SUB_TYPE_FRIEND: String = "friend"
    val PRIVATE: String = "private"
    val GROUP: String = "group"
  }
}
