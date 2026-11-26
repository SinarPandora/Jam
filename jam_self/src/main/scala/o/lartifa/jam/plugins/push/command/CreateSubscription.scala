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
        val obsId = await(getOrCreateObserver(sourceType, sourceIdentity))
        val exist = await(db.run {
          SourceSubscriber
            .filter(row =>
              row.sourceId === obsId
                && row.chatId === context.chatInfo.chatId
                && row.chatType === context.chatInfo.chatType)
            .exists
            .result
        })
        if (exist) {
          reply("源已被订阅🤔")
        } else {
          await(db.run {
            SourceSubscriber.map(row => (row.sourceId, row.chatId, row.chatType)) +=
              ((obsId, context.chatInfo.chatId, context.chatInfo.chatType))
          })
          reply(
            """订阅成功！🎉（消息将在稍后开始推送）
              |----------------------
              |你可以发送 .订阅 列表
              |来查看当前聊天的订阅信息""".stripMargin)
        }
      case _ =>
    }
    ()
  }(ThreadPools.DB)

  /**
   * 获取或创建源观察者
   *
   * @param sourceType     源类型
   * @param sourceIdentity 源标识
   * @return 观察者 ID
   */
  def getOrCreateObserver(sourceType: String, sourceIdentity: String): Future[Long] = async {
    await(getObserver(sourceType, sourceIdentity)) match {
      case Some(observer) => observer.id
      case None => await(db.run {
        SourceObserver
          .map(row => (row.sourceType, row.sourceIdentity))
          .returning(SourceObserver.map(_.id)) +=
          ((sourceType, sourceIdentity))
      })
    }
  }(ThreadPools.DB)
}
