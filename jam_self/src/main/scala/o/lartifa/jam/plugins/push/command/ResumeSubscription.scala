package o.lartifa.jam.plugins.push.command

import o.lartifa.jam.common.protocol.CommonProtocol
import o.lartifa.jam.common.util.ExtraActor
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.behaviors.ActorCreator
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.plugins.push.source.SourceIdentity
import o.lartifa.jam.plugins.push.subscriber.SourceSubscriber.SourceSubscriberProtocol
import o.lartifa.jam.plugins.push.{Prompts, SourcePushClient}

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * 恢复订阅
 *
 * Author: sinar
 * 2022/6/9 00:28
 */
object ResumeSubscription extends Command[Unit] {
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
        await(SourcePushClient.getSubscriptionOrPrompt(SourceIdentity(sourceType, sourceIdentity), context.chatInfo)) match {
          case Some(subscriber) =>
            ActorCreator.actorOf(ExtraActor(
              ctx => subscriber ! SourceSubscriberProtocol.Resume(ctx.self),
              _ => {
                case CommonProtocol.Done => reply("订阅已恢复")
              }
            ))
          case None => reply("订阅不存在！")
        }
      case (None, _) => reply(Prompts.PleaseProvideMoreInfoForCommand("恢复", "订阅源类型"))
      case (_, None) => reply(Prompts.PleaseProvideMoreInfoForCommand("恢复", "订阅源标识"))
    }
  }
}
