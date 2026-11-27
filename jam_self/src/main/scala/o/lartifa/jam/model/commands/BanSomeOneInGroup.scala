package o.lartifa.jam.model.commands

import java.util.concurrent.TimeUnit

import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.onebot.domain.{DiscussMessage, GroupMessage}
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 禁言某人指令
 *
 * Author: sinar
 * 2020/11/1 11:53
 */
case class BanSomeOneInGroup(qIdTemplate: RenderStrTemplate, timeTemplate: RenderStrTemplate, unit: TimeUnit) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] =
    Future.unit.flatMap { _ =>
      val groupId = context.eventMessage match {
        case message: GroupMessage => message.groupId
        case message: DiscussMessage => message.discussId
        case _ => throw ExecutionException("无法在非群聊进行禁言")
      }
      for {
        qId <- qIdTemplate.execute()
        time <- timeTemplate.execute()
      } yield {
        JamContext.apiClient.setGroupBan(groupId, qId.toLong, unit.toSeconds(time.toLong))
        ()
      }
    }
}
