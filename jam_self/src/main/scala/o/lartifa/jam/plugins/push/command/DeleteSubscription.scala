package o.lartifa.jam.plugins.push.command

import o.lartifa.jam.database.Memory.database.*
import o.lartifa.jam.database.Memory.database.profile.api.*
import o.lartifa.jam.database.schema.Tables.*
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.pool.ThreadPools

import scala.concurrent.{ExecutionContext, Future}

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
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] =
    run(context)(ThreadPools.DB)

  private def run(context: CommandExecuteContext)(implicit exec: ExecutionContext): Future[Unit] = {
    implicit val ctx: CommandExecuteContext = context
    for {
      sourceTypeOpt <- context.tempVars.get("sourceType")
      sourceIdentityOpt <- context.tempVars.get("sourceIdentity")
      _ <- (sourceTypeOpt, sourceIdentityOpt) match {
        case (Some(sourceType), Some(sourceIdentity)) =>
          getSubscriber(sourceType, sourceIdentity, context.chatInfo).flatMap {
            case Some(subscriber) =>
              for {
                _ <- db.run(SourceSubscriber.filter(_.id === subscriber.id).delete)
                _ <- db.run {
                  sqlu"""
                    delete
                    from source_observer obs
                    where not exists(
                            select sub.id from source_subscriber sub
                                          where source_id = obs.id)
                """
                }
              } yield reply("订阅已取消👋")
            case None =>
              reply(Prompts.SubscriptionNotExist)
              Future.unit
          }
        case _ => Future.unit
      }
    } yield ()
  }
}
