package o.lartifa.jam.cool.qq.listener.posthandle

import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 后置任务
 * 定义的任务会在 SSDL 后执行
 * 不建议直接实现该类，请使用 PostProcessedHandleTask 和 PostUnProcessedHandleTask
 *
 * Author: sinar
 * 2021/6/12 19:42
 *
 * @param name              后置指令名称
 * @param handleOnProcessed 该指令是否在消息处理后执行
 *                          此处值为 false 意味着传入的 指令执行上下文 参数为 None
 */
abstract class PostHandleTask(val name: String, val handleOnProcessed: Boolean = false) {

  /**
   * 执行
   *
   * @param event   消息对象
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  def execute(event: EventMessage, context: Option[CommandExecuteContext])(implicit exec: ExecutionContext): Future[Unit]
}
