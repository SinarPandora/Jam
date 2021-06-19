package o.lartifa.jam.cool.qq.listener.posthandle

import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.common.util.TriBoolValue
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 在消息未被捕获时执行的后置任务
 *
 * Author: sinar
 * 2021/6/12 21:48
 */
abstract class PostUnProcessedHandleTask(name: String) extends PostHandleTask(name, TriBoolValue.False) {
  /**
   * 执行
   *
   * @param event      消息对象
   * @param contextOpt 执行上下文
   * @param exec       异步上下文
   * @return 异步返回执行结果
   */
  override def execute(event: EventMessage, contextOpt: Option[CommandExecuteContext])(implicit exec: ExecutionContext): Future[Unit] =
    this.execute(event)

  /**
   * 执行
   *
   * @param event 消息对象
   * @param exec  异步上下文
   * @return 异步返回执行结果
   */
  def execute(event: EventMessage)(implicit exec: ExecutionContext): Future[Unit]
}
