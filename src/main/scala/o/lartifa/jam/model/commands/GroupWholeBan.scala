package o.lartifa.jam.model.commands
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}

import scala.concurrent.{ExecutionContext, Future}

/**
 * 全体禁言指令
 *
 * Author: sinar
 * 2020/1/24 00:26 
 */
case class GroupWholeBan(isBan: Boolean) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = Future {
    val ChatInfo(chatType, chatId) = ChatInfo(context.eventMessage)
    if (chatType != MessageType.GROUP) throw ExecutionException(s"尝试在非群聊中使用全体禁言，聊天类型：$chatType，QQ：$chatId")
    context.eventMessage.getHttpApi.setGroupWholeBan(chatId, isBan)
  }
}
