package o.lartifa.jam.cool.qq.listener.prehandle

import ammonite.ops.PipeableImplicit
import cc.moecraft.icq.event.events.message.{EventMessage, EventPrivateMessage}
import cc.moecraft.icq.sender.message.components.ComponentAt
import o.lartifa.jam.common.config.{JamCharacter, JamConfig}
import o.lartifa.jam.pool.JamContext

import scala.async.Async.async
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
 * 梦呓
 *
 * Author: sinar
 * 2020/9/11 21:38
 */
object JamIsSleeping extends PreHandleTask("梦呓") {
  private val balderdash: List[String] = JamCharacter.balderdash
  private lazy val atMyself: String = new ComponentAt(JamConfig.qID).toString

  /**
   * 执行前置任务
   *
   * @param event 消息对象（注意此时还没开始进行 SSDL 解析）
   * @param exec  异步上下文
   * @return 如果返回 false，将打断后续的 SSDL 执行
   */
  override def execute(event: EventMessage)(implicit exec: ExecutionContext): Future[Boolean] = async {
    if (JamContext.jamIsSleeping.get() && balderdash.nonEmpty && Random.nextInt(4) == 0) {
      if (event.isInstanceOf[EventPrivateMessage] || event.message.contains(atMyself)) {
        balderdash.size |> Random.nextInt |> balderdash |> event.respond
      }
    }
    true
  }
}
