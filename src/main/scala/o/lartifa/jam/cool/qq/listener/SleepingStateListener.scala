package o.lartifa.jam.cool.qq.listener

import ammonite.ops.PipeableImplicit
import cc.moecraft.icq.event.events.message.{EventMessage, EventPrivateMessage}
import cc.moecraft.icq.event.{EventHandler, IcqListener}
import cc.moecraft.icq.sender.message.components.ComponentAt
import o.lartifa.jam.common.config.{JamCharacter, JamConfig}

import scala.util.Random

/**
 * 睡觉时被戳来戳去，给与反馈
 *
 * Author: sinar
 * 2020/9/19 10:45
 */
object SleepingStateListener extends IcqListener {
  private val balderdash: List[String] = JamCharacter.balderdash
  private lazy val atMyself: String = new ComponentAt(JamConfig.qID).toString

  /**
   * 监听消息
   *
   * @param eventMessage 消息对象
   */
  @EventHandler
  def listen(eventMessage: EventMessage): Unit = {
    if (Random.nextInt(4) == 0) {
      if (eventMessage.isInstanceOf[EventPrivateMessage] || eventMessage.message.contains(atMyself)) {
        balderdash.size |> Random.nextInt |> balderdash |> eventMessage.respond
      }
    }
  }

}
