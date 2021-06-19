package o.lartifa.jam.cool.qq.listener.posthandle

import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.common.util.TriBoolValue
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command

import scala.concurrent.{ExecutionContext, Future}

/**
 * 在消息被捕获处理后的后置任务
 * 此时后置任务相当于一个空返回指令
 *
 * Author: sinar
 * 2021/6/12 21:43
 */
abstract class PostProcessedHandleTask(name: String) extends PostHandleTask(name, TriBoolValue.True)
  with Command[Unit] {
  /**
   * 执行
   *
   * @param event      消息对象
   * @param contextOpt 执行上下文
   * @param exec       异步上下文
   * @return 异步返回执行结果
   */
  override def execute(event: EventMessage, contextOpt: Option[CommandExecuteContext])(implicit exec: ExecutionContext): Future[Unit] =
    this.execute()(contextOpt.get, exec)
}
