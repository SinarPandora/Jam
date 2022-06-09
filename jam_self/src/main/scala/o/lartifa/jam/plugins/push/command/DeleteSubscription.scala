package o.lartifa.jam.plugins.push.command

import o.lartifa.jam.common.protocol.CommonProtocol
import o.lartifa.jam.common.util.ExtraActor
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.behaviors.ActorCreator
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.plugins.push.observer.SourceObserver.SourceObserverProtocol
import o.lartifa.jam.plugins.push.source.SourceIdentity
import o.lartifa.jam.plugins.push.{Prompts, SourcePushClient}

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future, Promise}

/**
 * 删除订阅
 *
 * Author: sinar
 * 2022/6/9 00:27
 */
object DeleteSubscription extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    (await(context.tempVars.get("sourceType")), await(context.tempVars.get("sourceIdentity"))) match {
      case (Some(sourceType), Some(sourceIdentity)) =>
        await(SourcePushClient.getObserver(SourceIdentity(sourceType, sourceIdentity))) match {
          case Some(observer) =>
            val promise = Promise[Unit]()
            ActorCreator.actorOf(ExtraActor(
              ctx => observer ! SourceObserverProtocol.CancelSubscriber(context.chatInfo, ctx.self),
              _ => {
                case CommonProtocol.Done =>
                  reply("订阅已取消")
                  promise.success(())
                case CommonProtocol.Fail(msg) =>
                  reply(msg)
                  promise.success(())
              }
            ))
            await(promise.future)
          case None => reply("订阅不存在")
        }
      case (None, _) => reply(Prompts.PleaseProvideMoreInfoForCommand("删除", "订阅源类型"))
      case (_, None) => reply(Prompts.PleaseProvideMoreInfoForCommand("删除", "订阅源标识"))
    }
  }
}
