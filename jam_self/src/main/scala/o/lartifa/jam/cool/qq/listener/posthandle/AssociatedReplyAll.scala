package o.lartifa.jam.cool.qq.listener.posthandle
import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.common.util.TriBoolValue
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 在任何情况下进行联想回复
 *
 * Author: sinar
 * 2021/6/13 03:53
 */
object AssociatedReplyAll extends PostHandleTask("联想回复（匹配后）", TriBoolValue.Both) {
  /**
   * 执行
   *
   * @param event   消息对象
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute(event: EventMessage, context: Option[CommandExecuteContext])(implicit exec: ExecutionContext): Future[Unit] =
    AssociatedReply.execute(event)
}
