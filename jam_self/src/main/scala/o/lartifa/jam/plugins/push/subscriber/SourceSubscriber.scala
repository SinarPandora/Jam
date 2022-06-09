package o.lartifa.jam.plugins.push.subscriber

import akka.actor.{Actor, ActorRef}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.protocol.CommonProtocol
import o.lartifa.jam.common.util.TimeUtil
import o.lartifa.jam.database.Memory.database.*
import o.lartifa.jam.database.Memory.database.profile.api.*
import o.lartifa.jam.database.schema.Tables.{SourcePushHistory, SourceSubscriber as SourceSubscriberTable}
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.plugins.push.source.SourceIdentity
import o.lartifa.jam.plugins.push.subscriber.SourceSubscriber.SourceSubscriberProtocol
import o.lartifa.jam.plugins.push.template.SourceContent
import o.lartifa.jam.pool.{JamContext, ThreadPools}

import scala.async.Async.{async, await}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * 源订阅者
 *
 * Author: sinar
 * 2022/5/2 19:02
 */
class SourceSubscriber(id: Long, chatInfo: ChatInfo, source: SourceIdentity, isPaused: Boolean = false, lastKey: String = "INIT") extends Actor {
  that =>
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(this.getClass)

  override def receive: Receive = if (isPaused) pauseStage(lastKey) else listenStage(lastKey)

  /**
   * 监听阶段
   *
   * @param lastKey 最后推送消息键
   * @return 行为
   */
  def listenStage(lastKey: String): Receive = {
    case SourceSubscriberProtocol.SourcePush(SourceContent(messageKey, renderResult)) =>
      if (messageKey != lastKey) {
        async {
          val pushed = await(db.run {
            SourcePushHistory
              .filter(row => row.subscriberId === id && row.messageKey === messageKey)
              .exists.result
          })
          if (!pushed) {
            import ChatInfo.ChatInfoReply
            chatInfo.sendMsg(renderResult.message)
            await(db.run {
              DBIO.sequence(Seq(
                SourceSubscriberTable
                  .filter(_.id === id)
                  .map(row => (row.lastKey, row.lastUpdateTime))
                  .update((messageKey, TimeUtil.currentTimeStamp)),
                SourcePushHistory.map(row => (row.subscriberId, row.messageKey)) += ((id, messageKey))
              ))
            })
          }
        }(ThreadPools.DB).onComplete {
          case Failure(exception) =>
            logger.error(s"推送出现错误，消息标识：$lastKey，消息内容：${renderResult.message}，消息源：$source，目标聊天：$chatInfo", exception)
          case Success(_) =>
            logger.debug(s"消息成功推送，消息标识：$lastKey，消息内容：${renderResult.message}，消息源：$source，目标聊天：$chatInfo")
            that.context.become(listenStage(messageKey))
        }(ThreadPools.SCHEDULE_TASK)
      }
    case SourceSubscriberProtocol.Pause(fromRef) =>
      db.run {
        SourceSubscriberTable
          .filter(_.id === id)
          .map(_.isPaused)
          .update(true)
      }
      that.context.become(pauseStage(lastKey))
      fromRef ! CommonProtocol.Done
    case CommonProtocol.IsAlive_?(fromRef) =>
      fromRef ! CommonProtocol.Online
  }

  /**
   * 暂停阶段
   *
   * @param lastKey 最后推送消息键
   * @return 行为
   */
  def pauseStage(lastKey: String): Receive = {
    case SourceSubscriberProtocol.Resume(fromRef) =>
      db.run {
        SourceSubscriberTable
          .filter(_.id === id)
          .map(_.isPaused)
          .update(false)
      }
      that.context.become(listenStage(lastKey))
      fromRef ! CommonProtocol.Done
  }
}

object SourceSubscriber {
  object SourceSubscriberProtocol {
    sealed trait Request
    // 源推送
    case class SourcePush(content: SourceContent) extends Request
    // 暂停接收消息
    case class Pause(fromRef: ActorRef) extends Request
    // 继续接收消息
    case class Resume(fromRef: ActorRef) extends Request
  }
}
