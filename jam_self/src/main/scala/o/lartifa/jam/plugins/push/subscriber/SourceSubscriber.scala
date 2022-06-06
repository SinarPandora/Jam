package o.lartifa.jam.plugins.push.subscriber

import akka.actor.{Actor, ActorRef}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.protocol.CommonProtocol
import o.lartifa.jam.common.util.TimeUtil
import o.lartifa.jam.database.Memory.database.*
import o.lartifa.jam.database.Memory.database.profile.api.*
import o.lartifa.jam.database.schema.Tables.SourceSubscriber as SourceSubscriberTable
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.plugins.push.source.SourceIdentity
import o.lartifa.jam.plugins.push.subscriber.SourceSubscriber.{SourceSubscriberData, SourceSubscriberProtocol}
import o.lartifa.jam.plugins.push.template.SourceContent
import o.lartifa.jam.pool.{JamContext, ThreadPools}

import scala.concurrent.Future
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

  override def receive: Receive = listenStage(SourceSubscriberData(isPaused, lastKey))

  /**
   * 监听阶段
   *
   * @param data 源订阅者数据
   * @return 行为
   */
  def listenStage(data: SourceSubscriberData): Receive = {
    case SourceSubscriberProtocol.SourcePush(SourceContent(messageKey, renderResult)) =>
      if (messageKey != data.lastKey) {
        Future
          .unit
          .flatMap { _ =>
            import ChatInfo.ChatInfoReply
            chatInfo.sendMsg(renderResult.message)
            db.run {
              SourceSubscriberTable
                .filter(_.id === id)
                .map(row => (row.lastKey, row.lastUpdateTime))
                .update((messageKey, TimeUtil.currentTimeStamp))
            }
          }(ThreadPools.SCHEDULE_TASK)
          .onComplete {
            case Failure(exception) =>
              logger.error(s"推送出现错误，消息标识：$lastKey，消息内容：${renderResult.message}，消息源：$source，目标聊天：$chatInfo", exception)
            case Success(_) =>
              logger.debug(s"消息成功推送，消息标识：$lastKey，消息内容：${renderResult.message}，消息源：$source，目标聊天：$chatInfo")
          }(ThreadPools.SCHEDULE_TASK)
      }
    case SourceSubscriberProtocol.Pause(fromRef) =>
      that.context.become(pauseStage(data))
      fromRef ! CommonProtocol.Done
    case CommonProtocol.IsAlive_?(fromRef) =>
      fromRef ! CommonProtocol.Online
  }

  /**
   * 暂停阶段
   *
   * @param data 源订阅者数据
   * @return 行为
   */
  def pauseStage(data: SourceSubscriberData): Receive = {
    case SourceSubscriberProtocol.Resume(fromRef) =>
      that.context.become(listenStage(data))
      fromRef ! CommonProtocol.Done
  }
}

object SourceSubscriber {
  case class SourceSubscriberData(isPaused: Boolean, lastKey: String)
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
