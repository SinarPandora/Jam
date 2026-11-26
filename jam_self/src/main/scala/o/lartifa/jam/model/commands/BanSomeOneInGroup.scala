package o.lartifa.jam.model.commands

import java.util.concurrent.TimeUnit

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventPrivateMessage}
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.CommandExecuteContext

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
      if (context.eventMessage.isInstanceOf[EventPrivateMessage]) {
        throw ExecutionException("无法在非群聊进行禁言")
      }
      for {
        qId <- qIdTemplate.execute()
        time <- timeTemplate.execute()
      } yield {
        context.eventMessage.getHttpApi.setGroupBan(
          context.eventMessage.asInstanceOf[EventGroupOrDiscussMessage].getGroup.getId, qId.toLong, unit.toSeconds(time.toLong)
        )
        ()
      }
    }
}
