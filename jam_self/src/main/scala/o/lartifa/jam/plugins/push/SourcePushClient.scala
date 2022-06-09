package o.lartifa.jam.plugins.push

import akka.actor.ActorRef
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.common.protocol.CommonProtocol
import o.lartifa.jam.common.util.ExtraActor
import o.lartifa.jam.model.behaviors.{ActorCreator, ReplyToFriend}
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.plugins.push.observer.ObserverRegistry.ObserverRegistryProtocol
import o.lartifa.jam.plugins.push.observer.SourceObserver.SourceObserverProtocol
import o.lartifa.jam.plugins.push.source.SourceIdentity
import o.lartifa.jam.pool.JamContext

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future, Promise}

/**
 * 源订阅客户端
 *
 * Author: sinar
 * 2022/6/9 23:33
 */
object SourcePushClient extends ReplyToFriend {
  /**
   * 获取订阅源
   *
   * @param sourceIdentity 订阅源标识
   * @param context        指令上下文
   * @param exec           异步上下文
   * @return
   */
  def getObserver(sourceIdentity: SourceIdentity)(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Option[ActorRef]] = async {
    val obsPromise = Promise[Option[ActorRef]]()
    ActorCreator.actorOf(ExtraActor(
      ctx => JamContext.observerRegistry.get() ! ObserverRegistryProtocol.Search(sourceIdentity, ctx.self),
      _ => {
        case ObserverRegistryProtocol.Created(observer) => obsPromise.success(Some(observer))
        case ObserverRegistryProtocol.NotFound => obsPromise.success(None)
      }
    ))
    await(obsPromise.future)
  }

  /**
   * 获取或创建订阅源
   * * 创建失败将输出提示信息
   *
   * @param sourceIdentity 订阅源标识
   * @param context        指令上下文
   * @param exec           异步上下文
   * @return
   */
  def getOrElseCreateObserver(sourceIdentity: SourceIdentity)(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[ActorRef] = async {
    val promise = Promise[ActorRef]()
    ActorCreator.actorOf(ExtraActor(
      ctx => JamContext.observerRegistry.get() ! ObserverRegistryProtocol.SearchOrCreate(sourceIdentity, ctx.self),
      _ => {
        case ObserverRegistryProtocol.Created(observer) =>
          promise.success(observer)
        case ObserverRegistryProtocol.Found(observer) =>
          promise.success(observer)
        case CommonProtocol.Fail(msg) =>
          reply(msg)
          promise.failure(ExecutionException("订阅源创建失败"))
      }
    ))
    await(promise.future)
  }

  /**
   * 创建订阅
   * * 创建失败时输出提示信息
   *
   * @param observer 源观察者
   * @param chatInfo 会话信息
   * @param context  指令上下文
   * @param exec     异步上下文
   * @return
   */
  def createSubscriptionOrPrompt(observer: ActorRef, chatInfo: ChatInfo)(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    val promise = Promise[Unit]()
    ActorCreator.actorOf(ExtraActor(
      ctx => observer ! SourceObserverProtocol.AddSubscriber(context.chatInfo, ctx.self),
      _ => {
        case CommonProtocol.Fail(msg) =>
          reply(msg)
          promise.failure(ExecutionException("订阅创建失败"))
        case CommonProtocol.Done =>
          promise.success(())
      }
    ))
    await(promise.future)
  }

  /**
   * 获取订阅
   * 不存在时输出提示信息
   *
   * @param sourceIdentity 订阅源标识
   * @param chatInfo       会话信息
   * @param context        指令上下文
   * @param exec           执行上下文
   * @return
   */
  def getSubscriptionOrPrompt(sourceIdentity: SourceIdentity, chatInfo: ChatInfo)(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Option[ActorRef]] = async {
    await(getObserver(sourceIdentity)) match {
      case Some(observer) =>
        val promise = Promise[Option[ActorRef]]()
        ActorCreator.actorOf(ExtraActor(
          ctx => observer ! SourceObserverProtocol.GetSubscriber(context.chatInfo, ctx.self),
          _ => {
            case SourceObserverProtocol.Found(ref) =>
              promise.success(Some(ref))
            case SourceObserverProtocol.NotFound =>
              reply(Prompts.NotFoundPleaseCreate(sourceIdentity))
              promise.success(None)
          }
        ))
        await(promise.future)
      case None =>
        reply(Prompts.NotFoundPleaseCreate(sourceIdentity))
        None
    }
  }
}
