package o.lartifa.jam.plugins.rss

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * 取消订阅指令
 *
 * Author: sinar
 * 2020/9/3 00:54
 */
object RSSUnSubscribe extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    import context.eventMessage._
    if (!message.contains(" ")) this.reply(s"$atSender 请在订阅源前面加个空格~")
    else {
      val source = message.split(" ").last.trim
      await(SubscriptionPool.unSubscribeAndReply(source, context.chatInfo))
    }
  }
}
