package o.lartifa.jam.plugins.push.subscriber

import akka.actor.{Actor, ActorRef, Cancellable}
import o.lartifa.jam.common.protocol.CommonProtocol
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.plugins.push.observer.SourceObserver.SourceContent
import o.lartifa.jam.plugins.push.subscriber.SourceSubscriber.SourceSubscriberProtocol
import o.lartifa.jam.pool.ThreadPools

import scala.concurrent.duration.*
import scala.language.postfixOps

/**
 * 源订阅者
 *
 * Author: sinar
 * 2022/5/2 19:02
 */
class SourceSubscriber(id: Long, data: ChatInfo) extends Actor {
  that =>

  override def preStart(): Unit = {
    val task = this.context.system.scheduler.scheduleAtFixedRate(
      initialDelay = 1 seconds,
      interval = 3 minutes,
      receiver = that.self,
      message = SourceSubscriberProtocol.SelfCheck
    )(ThreadPools.SCHEDULE_TASK)
    that.context.become(listenStage(Some(task)))
  }

  override def receive: Receive = listenStage(None)

  /**
   * 监听阶段
   *
   * @param data     自身记录
   * @param scanTask 扫描任务
   * @return 行为
   */
  def listenStage(scanTask: Option[Cancellable]): Receive = {
    case SourceSubscriberProtocol.SourcePush(content, fromRef) =>
      fromRef ! CommonProtocol.Done
    case SourceSubscriberProtocol.Pause(fromRef) =>
      ???
    case SourceSubscriberProtocol.Resume(fromRef) =>
      ???
    case SourceSubscriberProtocol.SelfCheck =>
      ???
    case CommonProtocol.IsAlive_?(fromRef) =>
      fromRef ! CommonProtocol.Online
  }

  def push() = ???
}

object SourceSubscriber {
  object SourceSubscriberProtocol {
    sealed trait Request
    // 源推送
    case class SourcePush(content: SourceContent, fromRef: ActorRef) extends Request
    // 暂停接收消息
    case class Pause(fromRef: ActorRef) extends Request
    // 继续接收消息
    case class Resume(fromRef: ActorRef) extends Request
    // 自检（检查是否有消息发送权限）
    case object SelfCheck extends Request
  }
}
