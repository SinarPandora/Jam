package o.lartifa.jam.plugins.rss

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command

import scala.concurrent.{ExecutionContext, Future}

/**
 * 列出全部订阅指令
 *
 * Author: sinar
 * 2020/9/3 00:54
 */
object RSSShowAll extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] =
    SubscriptionPool.listSubscriptionsAndReply(context.chatInfo)
}
