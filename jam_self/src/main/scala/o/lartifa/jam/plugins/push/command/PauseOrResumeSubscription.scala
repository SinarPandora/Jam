package o.lartifa.jam.plugins.push.command

import o.lartifa.jam.database.Memory.database.*
import o.lartifa.jam.database.Memory.database.profile.api.*
import o.lartifa.jam.database.schema.Tables.*
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.pool.ThreadPools

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
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] =
    run(context)(ThreadPools.DB)

  private def run(context: CommandExecuteContext)(implicit exec: ExecutionContext): Future[Unit] = {
    implicit val ctx: CommandExecuteContext = context
    for {
      sourceTypeOpt <- context.tempVars.get("sourceType")
      sourceIdentityOpt <- context.tempVars.get("sourceIdentity")
      isPausedOpt <- context.tempVars.get("isPaused")
      _ <- (sourceTypeOpt, sourceIdentityOpt, isPausedOpt) match {
        case (Some(sourceType), Some(sourceIdentity), Some(isPausedStr)) =>
          getSubscriber(sourceType, sourceIdentity, context.chatInfo).flatMap {
            case Some(subscriber) =>
              val isPaused = isPausedStr.toBoolean
              db.run {
                SourceSubscriber.filter(_.id === subscriber.id)
                  .map(_.isPaused)
                  .update(isPaused)
              }.map { _ =>
                reply(if (isPaused) "已暂停订阅⏸" else "已恢复订阅▶️")
              }
            case None =>
              reply(Prompts.SubscriptionNotExist)
              Future.unit
          }
        case _ => Future.unit
      }
    } yield ()
  }
}
