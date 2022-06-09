package o.lartifa.jam.plugins.push.command

import o.lartifa.jam.database.Memory.database.*
import o.lartifa.jam.database.Memory.database.profile.api.*
import o.lartifa.jam.database.schema.Tables.{SourceObserver, SourceSubscriber}
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * 列出订阅
 *
 * Author: sinar
 * 2022/6/8 22:37
 */
object ListSubscriptionsInChat extends Command[Int] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Int] = async {
    val ChatInfo(chatType, chatId) = context.chatInfo
    val query = db.run {
      SourceObserver
        .join(SourceSubscriber).on((obs, sub) => obs.id === sub.sourceId)
        .filter {
          case (_, subscriber) => subscriber.chatType === chatType && subscriber.chatId === chatId
        }
        .map {
          case (observer, subscriber) => (observer.sourceType, observer.sourceIdentity, subscriber.isPaused)
        }
        .result
    }
    val sources = await(query).map {
      case (sourceType, sourceIdentity, isPaused) =>
        s"[${if (isPaused) "已暂停" else "订阅中"}] $sourceType：$sourceIdentity"
    }
    if (sources.isEmpty) {
      reply(
        """当前没有任何订阅
          |------------------------
          |请使用 .订阅 添加 指令来添加订阅""".stripMargin)
    } else {
      reply(
        s"""已订阅列表
           |------------------------
           |${sources.mkString("\n")}
           |------------------------
           |暂停订阅：.订阅 暂停
           |恢复订阅：.订阅 恢复
           |删除订阅：.订阅 删除""".stripMargin)
    }
    sources.size
  }
}
