package o.lartifa.jam.cool.qq.listener.interactive

import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.common.util.ExtraActor
import o.lartifa.jam.cool.qq.listener.interactive.InteractiveSessionProtocol.Manage
import o.lartifa.jam.model.SpecificSender
import o.lartifa.jam.model.behaviors.ActorCreator

import scala.concurrent.{Future, Promise}

/**
 * 交互式会话监听器
 *
 * Author: sinar
 * 2021/8/19 01:40
 */
object InteractiveSessionListener {
  /**
   * 当存在针对的会话时，触发交互
   *
   * @param event 消息事件
   * @return 当未找到时，返回 true 表示消息由其他监听器继续处理，找到时返回 false
   */
  def blockAndInactiveIfExist(event: EventMessage): Future[Boolean] = {
    val promise: Promise[Boolean] = Promise()
    ActorCreator.actorOf(ExtraActor(
      ctx => manager ! Manage.Search(SpecificSender(event), ctx.self),
      _ => {
        case Manage.Found(ref) =>
          ref ! event
          promise.success(false)
        case Manage.NotFound =>
          promise.success(true)
      }
    ))
    promise.future
  }
}
