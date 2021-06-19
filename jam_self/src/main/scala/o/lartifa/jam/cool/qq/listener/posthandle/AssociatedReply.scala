package o.lartifa.jam.cool.qq.listener.posthandle

import cc.moecraft.icq.event.events.message.EventMessage

import scala.concurrent.{ExecutionContext, Future}

/**
 * 后置任务：联想回复
 * 若消息内容包含艾特 bot，并且没有被捕获，则联想消息内容并给出回复
 *
 * Author: sinar
 * 2021/6/12 21:29
 */
object AssociatedReply extends PostUnProcessedHandleTask("联想回复") {

  /**
   * 执行
   *
   * @param event 消息对象
   * @param exec  异步上下文
   * @return 异步返回执行结果
   */
  override def execute(event: EventMessage)(implicit exec: ExecutionContext): Future[Unit] =
    AssociatedReplyAll.execute(event, None)
}
