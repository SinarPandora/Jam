package o.lartifa.jam.cool.qq.listener.prehandle

import cc.moecraft.icq.event.events.message.EventMessage

import scala.concurrent.{ExecutionContext, Future}

/**
 * 前置任务
 * 定义的任务将在 SSDL 执行前被执行
 *
 * Author: sinar
 * 2020/8/29 21:43
 */
abstract class PreHandleTask(val name: String) {
  /**
   * 执行前置任务
   *
   * @param event 消息对象（注意此时还没开始进行 SSDL 解析）
   * @param exec 异步上下文
   * @return 如果返回 false，将打断后续的 SSDL 执行
   */
  def execute(event: EventMessage)(implicit exec: ExecutionContext): Future[Boolean]
}
