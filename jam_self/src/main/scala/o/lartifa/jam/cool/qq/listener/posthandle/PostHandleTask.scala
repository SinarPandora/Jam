package o.lartifa.jam.cool.qq.listener.posthandle

import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.common.util.TriBoolValue
import o.lartifa.jam.common.util.TriBoolValue.TriBool
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 后置任务
 * 定义的任务会在 SSDL 后执行
 * 若确定只在消息被捕获/未捕获时触发，请使用 PostProcessedHandleTask 和 PostUnProcessedHandleTask
 *
 * Author: sinar
 * 2021/6/12 19:42
 *
 * @param name              后置指令名称
 * @param handleOnProcessed 该指令是否在消息处理后执行
 *                          此处值为 TriBool.False 意味着传入的 指令执行上下文 参数为 None
 *                          此处值为 TriBool.Both，则会同时在捕获和未捕获的情况下执行
 */
abstract class PostHandleTask(val name: String, val handleOnProcessed: TriBool = TriBoolValue.Both) {

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
