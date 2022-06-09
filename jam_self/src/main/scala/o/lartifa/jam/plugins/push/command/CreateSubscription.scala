package o.lartifa.jam.plugins.push.command

import akka.actor.ActorRef
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.plugins.push.source.SourceIdentity
import o.lartifa.jam.plugins.push.{Prompts, SourcePushClient}

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * 创建订阅
 *
 * Author: sinar
 * 2022/6/8 23:05
 */
object CreateSubscription extends Command[Unit] {
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
        val observer: ActorRef = await(SourcePushClient.getOrElseCreateObserver(SourceIdentity(sourceType, sourceIdentity)))
        await(SourcePushClient.createSubscriptionOrPrompt(observer, context.chatInfo))
        reply("订阅成功！")
      case (None, _) => reply(Prompts.PleaseProvideMoreInfoForCommand("添加", "订阅源类型"))
      case (_, None) => reply(Prompts.PleaseProvideMoreInfoForCommand("添加", "订阅源标识"))
    }
  }
}
