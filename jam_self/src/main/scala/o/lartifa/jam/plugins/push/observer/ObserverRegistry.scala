package o.lartifa.jam.plugins.push.observer

import akka.actor.{Actor, ActorRef, Props}
import cc.moecraft.logger.{HyLogger, LogLevel}
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.common.protocol.CommonProtocol
import o.lartifa.jam.common.util.{ExtraActor, MasterUtil}
import o.lartifa.jam.database.Memory.database.*
import o.lartifa.jam.database.Memory.database.profile.api.*
import o.lartifa.jam.database.schema.Tables.*
import o.lartifa.jam.plugins.push.observer.ObserverRegistry.{ObserverRegistryProtocol, logger, observerPrototypes}
import o.lartifa.jam.plugins.push.observer.SourceObserver as SourceObserverProto
import o.lartifa.jam.plugins.push.observer.SourceObserver.SourceObserverProtocol
import o.lartifa.jam.plugins.push.source.SourceIdentity
import o.lartifa.jam.pool.{JamContext, ThreadPools}

import scala.async.Async.{async, await}

/**
 * 订阅源注册
 *
 * Author: sinar
 * 2022/5/7 12:10
 */
class ObserverRegistry(parentRef: ActorRef) extends Actor {
  that =>
  override def receive: Receive = waitingObserversInitStage(Map.empty)

  override def preStart(): Unit = that.init(parentRef)

  /**
   * 等待订阅者初始化阶段
   *
   * @param registry 源注册表
   * @param inited   初始化次数
   * @return 行为
   */
  def waitingObserversInitStage(registry: Map[SourceIdentity, ActorRef], inited: Int = 0): Receive = {
    case CommonProtocol.Online =>
      if (registry.sizeIs == (inited + 1)) {
        that.context.become(listenStage(registry))
        parentRef ! CommonProtocol.Done
      } else {
        that.context.become(waitingObserversInitStage(registry, inited + 1))
      }
    case SourceObserverProtocol.Fail(identity, msg) =>
      MasterUtil.notifyAndLog(s"%s，源订阅初始化失败，${identity.info}，${JamConfig.config.name}将忽略该订阅源，错误信息：$msg", LogLevel.ERROR)
      that.context.become(waitingObserversInitStage(registry, inited + 1))
  }

  /**
   * 监听阶段
   *
   * @param registry 源注册表
   * @param creating 创建中的源订阅者
   * @return 行为
   */
  def listenStage(registry: Map[SourceIdentity, ActorRef], creating: Map[SourceIdentity, ActorRef] = Map.empty): Receive = {
    case ObserverRegistryProtocol.Search(identity, fromRef) =>
      fromRef ! registry.get(identity)
        .map(ObserverRegistryProtocol.Found.apply)
        .getOrElse(ObserverRegistryProtocol.NotFound)
    case ObserverRegistryProtocol.SearchOrCreate(identity, fromRef) =>
      registry.get(identity) match {
        case Some(ref) => fromRef ! ObserverRegistryProtocol.Found(ref)
        case None => that.createObserver(identity, fromRef, registry, creating)
      }
    case ObserverRegistryProtocol.PauseAll(from) =>
      (registry ++ creating).values.foreach(ref => ref ! SourceObserverProtocol.Resume(from))
    case ObserverRegistryProtocol.ResumeAll(from) =>
      (registry ++ creating).values.foreach(ref => ref ! SourceObserverProtocol.Resume(from))
  }

  /**
   * 初始化
   *
   * @param fromRef 消息来源（系统）
   */
  def init(fromRef: ActorRef): Unit = async {
    val observers = await(db.run(SourceObserver.result))
    val registry = observers.flatMap { it =>
      observerPrototypes.get(it.sourceType).map { proto =>
        SourceIdentity(it.sourceType, it.sourceIdentity) -> that.context.actorOf(Props(
          proto.getDeclaredConstructor(SourceObserverRow.getClass).newInstance(it)
        ))
      }.orElse {
        MasterUtil.notifyAndLog(s"%s，检测到未知的订阅类型：${it.sourceType}，请确认软件是否为最新版本",
          logLevel = LogLevel.WARNING)
        None
      }
    }.toMap
    that.context.become(waitingObserversInitStage(registry))
    registry.values.foreach(_ ! CommonProtocol.IsAlive_?(self))
  }(ThreadPools.DB)

  /**
   * 创建 Observer
   *
   * @param identity 源标识
   * @param fromRef  消息来源
   * @param registry 源注册表
   * @param creating 创建中的 Observer
   */
  def createObserver(identity: SourceIdentity, fromRef: ActorRef, registry: Map[SourceIdentity, ActorRef], creating: Map[SourceIdentity, ActorRef]): Unit = {
    val proto = observerPrototypes.getOrElse(identity.sourceIdentity, return fromRef ! CommonProtocol.Fail("该订阅类型不存在"))
    if (creating.contains(identity)) {
      fromRef ! CommonProtocol.Fail("源正在创建中")
      return
    }
    val observer = that.context.actorOf(Props(proto))
    that.context.become(this.listenStage(registry, creating + (identity -> observer)))
    that.context.actorOf(ExtraActor(
      ctx => {
        observer ! CommonProtocol.IsAlive_?(ctx.self)
      },
      _ => {
        case CommonProtocol.Online =>
          that.context.become(
            that.listenStage(registry + (identity -> observer))
          )
          logger.log(s"新源已创建：${identity.info}")
          fromRef ! ObserverRegistryProtocol.Created(observer)
          that.context.become(that.listenStage(registry + (identity -> observer), creating - identity))
        case SourceObserverProtocol.Fail(identity, msg) =>
          logger.error(s"资源观察者启动失败，${identity.info}，错误信息：$msg")
          fromRef ! CommonProtocol.Fail("订阅启动失败，请稍后重试")
          MasterUtil.notifyAndLog(s"%s，源订阅初始化失败，${identity.info}，${JamConfig.config.name}将忽略该订阅源", LogLevel.ERROR)
      }
    ))
  }
}

object ObserverRegistry {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(classOf[ObserverRegistry])
  val observerPrototypes: Map[String, Class[? <: SourceObserverProto]] = Map()
  object ObserverRegistryProtocol {
    // 请求
    sealed trait Request
    // 初始化
    case class Init(fromRef: ActorRef) extends Request
    // 搜索订阅源
    case class Search(identity: SourceIdentity, fromRef: ActorRef) extends Request
    // 搜索或创建
    case class SearchOrCreate(identity: SourceIdentity, fromRef: ActorRef) extends Request
    // 暂停全部订阅源扫描（夜间任务）
    case class PauseAll(from: ActorRef) extends Request
    // 恢复全部订阅源扫描（夜间任务）
    case class ResumeAll(from: ActorRef) extends Request
    // 响应
    sealed trait Response
    // 已找到
    case class Found(observerRef: ActorRef) extends Response
    // 已创建
    case class Created(observerRef: ActorRef) extends Response
    // 未找到
    case object NotFound extends Response
    // 其他引用 CommonProtocol.scala
  }
}
