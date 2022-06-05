package o.lartifa.jam.plugins.push.observer

import akka.actor.{Actor, ActorRef, Cancellable, PoisonPill, Props}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.protocol.CommonProtocol
import o.lartifa.jam.common.util.ExtraActor
import o.lartifa.jam.database.Memory.database.*
import o.lartifa.jam.database.Memory.database.profile.api.*
import o.lartifa.jam.database.schema.Tables.*
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.plugins.push.observer.SourceObserver.{SourceContent, SourceObserverData, SourceObserverProtocol}
import o.lartifa.jam.plugins.push.source.SourceIdentity
import o.lartifa.jam.plugins.push.subscriber.SourceSubscriber as SourceSubscriberProto
import o.lartifa.jam.plugins.push.subscriber.SourceSubscriber.SourceSubscriberProtocol
import o.lartifa.jam.plugins.push.template.TemplateRender.RenderResult
import o.lartifa.jam.plugins.push.template.{RenderRegistry, TemplateRender}
import o.lartifa.jam.pool.{JamContext, ThreadPools}

import scala.async.Async.{async, await}
import scala.concurrent.duration.*
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * 源观察者
 *
 * Author: sinar
 * 2022/5/7 12:06
 */
abstract class SourceObserver(initData: SourceObserverRow) extends Actor {
  that =>

  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(classOf[SourceObserver])

  override def preStart(): Unit = async {
    val subscribers = await(db.run(SourceSubscriber.filter(_.sourceId === initData.id).result))
    that.context.become(
      waitingForInitStage(
        subscribers
          .map(it => {
            val chatInfo = ChatInfo(it.chatType, it.chatId)
            chatInfo -> that.context.actorOf(Props(new SourceSubscriberProto(it.id, chatInfo)))
          })
          .toMap
      )
    )
  }(ThreadPools.DB)

  override def receive: Receive = waitingForInitStage()

  /**
   * 等待初始化阶段
   *
   * @param subscribers 订阅者
   * @param inited      初始化数量
   * @return 行为
   */
  def waitingForInitStage(subscribers: Map[ChatInfo, ActorRef] = Map(), inited: Int = 0): Receive = {
    case CommonProtocol.Online =>
      if (subscribers.sizeIs == inited + 1) {
        that.context.become(listenStage(
          SourceObserverData(
            subscribers = subscribers,
            sourceRender = RenderRegistry.get(initData.sourceIdentity),
            source = SourceIdentity(
              sourceType = initData.sourceType,
              sourceIdentity = initData.sourceIdentity
            ),
            scanTask = that.context.system.scheduler.scheduleAtFixedRate(
              initialDelay = 1 second,
              interval = 3 minutes,
              receiver = that.self,
              message = SourceObserver.SourceObserverProtocol.SourceScan,
            )(ThreadPools.SCHEDULE_TASK),
          )
        ))
        logger.log("订阅源：[%s] 加载完成", initData.sourceType)
      } else {
        that.context.become(waitingForInitStage(subscribers, inited + 1))
      }
  }

  /**
   * 监听阶段
   *
   * @param data 源观察者数据
   * @return 行为
   */
  def listenStage(data: SourceObserverData): Receive = {
    case SourceObserverProtocol.AddSubscriber(chatInfo, fromRef) =>
      if (data.subscribers.contains(chatInfo)) {
        fromRef ! CommonProtocol.Fail("该订阅源已被订阅")
      } else {
        db
          .run {
            SourceSubscriber
              .map(row => (row.chatId, row.chatType, row.sourceId))
              .returning(SourceSubscriber.map(_.id))
              += (chatInfo.chatId, chatInfo.chatType, initData.id)
          }
          .onComplete {
            case Failure(err) =>
              fromRef ! CommonProtocol.Fail("创建订阅失败，请稍后重试")
              logger.error(s"订阅取消失败，请检数据库，订阅信息：${data.source}，聊天信息：$chatInfo", err)
            case Success(id: Long) =>
              that.context.actorOf(ExtraActor(
                _ => that.context.actorOf(Props(new SourceSubscriberProto(id, chatInfo))),
                _ => {
                  case CommonProtocol.Online =>

                    fromRef ! CommonProtocol.Done
                },
              ))
          }(ThreadPools.DEFAULT)
      }
    case SourceObserverProtocol.CancelSubscriber(chatInfo, fromRef) =>
      data.subscribers.get(chatInfo) match {
        case Some(subscriber) =>
          that.context.stop(subscriber)
          db
            .run {
              SourceSubscriber
                .filter(row =>
                  row.sourceId === initData.id
                    && row.chatId === chatInfo.chatId
                    && row.chatType === chatInfo.chatType)
                .delete
            }
            .onComplete {
              case Failure(err) =>
                fromRef ! CommonProtocol.Fail("取消订阅失败，请稍后重试")
                logger.error(s"订阅取消失败，请检数据库，订阅信息：${data.source}，聊天信息：$chatInfo", err)
              case Success(_) =>
                if (data.subscribers.sizeIs == 1) {
                  logger.log(s"订阅源已无人订阅，订阅进程即将结束，订阅源：${data.source}")
                  that.self ! PoisonPill
                } else {
                  logger.log(s"已添加订阅源到：$chatInfo，订阅源信息：${data.source}")
                  that.context.become(listenStage(data.copy(subscribers = data.subscribers - chatInfo)))
                }
                fromRef ! CommonProtocol.Done
            }(ThreadPools.DEFAULT)
        case None => CommonProtocol.Fail(
          s"""当前聊天并没有订阅这个订阅源：
             |${data.source}""".stripMargin)
      }
    case SourceObserverProtocol.SourceScan => async {
      pull(data.sourceRender).foreach { it =>
        data.subscribers.values
          .foreach(sr => sr ! SourceSubscriberProtocol.SourcePush(it, that.self))
      }
    }(ThreadPools.SCHEDULE_TASK)
    case SourceObserverProtocol.Pause(fromRef) =>
      that.context.become(pauseStage(data))
      fromRef ! CommonProtocol.Done

  }

  /**
   * 暂停阶段
   *
   * @param data 源观察者数据
   * @return 行为
   */
  def pauseStage(data: SourceObserverData): Receive = {
    case SourceObserver.SourceObserverProtocol.Resume(fromRef) =>
      that.context.become(listenStage(data))
      fromRef ! CommonProtocol.Done
  }

  /**
   * 拉取消息
   *
   * @param render 模板渲染器
   * @return 拉取结果
   */
  def pull(render: TemplateRender): Option[SourceContent]
}

object SourceObserver {
  case class SourceObserverData
  (
    subscribers: Map[ChatInfo, ActorRef],
    sourceRender: TemplateRender,
    source: SourceIdentity,
    scanTask: Cancellable
  )
  case class SourceContent(messageKey: String, renderResult: RenderResult)
  // 协议
  object SourceObserverProtocol {
    sealed trait Request
    // 添加订阅者
    case class AddSubscriber(chatInfo: ChatInfo, fromRef: ActorRef) extends Request
    // 取消订阅者
    case class CancelSubscriber(chatInfo: ChatInfo, fromRef: ActorRef) extends Request
    // 源扫描（定时任务）
    case object SourceScan extends Request
    // 暂停扫描
    case class Pause(fromRef: ActorRef) extends Request
    // 恢复扫描
    case class Resume(fromRef: ActorRef) extends Request
    sealed trait Response
    case class Fail(identity: SourceIdentity, msg: String) extends Response
  }
}
