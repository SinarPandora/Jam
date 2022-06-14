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
        await(getSubscriber(sourceType, sourceIdentity, context.chatInfo)) match {
          case Some(subscriber) =>
            await(db.run {
              SourceSubscriber.filter(_.id === subscriber.id).delete
            })
            // 如果已经没有对应的订阅者，将源删除
            await(db.run {
              sqlu"""
                    delete
                    from source_observer obs
                    where not exists(
                            select sub.id from source_subscriber sub
                                          where source_id = obs.id)
                """
            })
            reply("订阅已取消👋")
          case None => reply(Prompts.SubscriptionNotExist)
        }
      case _ =>
    }
    ()
  }(ThreadPools.DB)
}
