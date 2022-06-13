package o.lartifa.jam.plugins.push.command

import o.lartifa.jam.database.Memory.database.*
import o.lartifa.jam.database.Memory.database.profile.api.*
import o.lartifa.jam.database.schema.Tables.*
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.pool.ThreadPools

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * 暂停或恢复订阅
 *
 * Author: sinar
 * 2022/6/9 00:28
 */
object PauseOrResumeSubscription extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    (await(context.tempVars.get("sourceType")),
      await(context.tempVars.get("sourceIdentity")),
      await(context.tempVars.get("isPaused"))) match {
      case (Some(sourceType), Some(sourceIdentity), Some(isPausedStr)) =>
        await(getSubscriber(sourceType, sourceIdentity, context.chatInfo)) match {
          case Some(subscriber) =>
            val isPaused = isPausedStr.toBoolean
            await(db.run {
              SourceSubscriber.filter(_.id === subscriber.id)
                .map(_.isPaused)
                .update(isPaused)
            })
            reply(if (isPaused) "已暂停订阅⏸" else "已恢复订阅▶️")
          case None => reply(Prompts.SubscriptionNotExist)
        }
      case _ =>
    }
    ()
  }(ThreadPools.DB)
}
