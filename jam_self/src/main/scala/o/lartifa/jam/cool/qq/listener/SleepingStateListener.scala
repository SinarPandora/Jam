package o.lartifa.jam.cool.qq.listener

import cc.moecraft.icq.event.events.message.{EventMessage, EventPrivateMessage}
import cc.moecraft.icq.event.{EventHandler, IcqListener}
import cc.moecraft.icq.sender.message.components.ComponentAt
import o.lartifa.jam.common.config.{BotConfig, JamConfig}

import scala.util.Random
import scala.util.chaining.*

/**
 * 睡觉时被戳来戳去，给与反馈
 *
 * Author: sinar
 * 2020/9/19 10:45
 */
object SleepingStateListener extends IcqListener {
  private def balderdash: List[String] = JamConfig.config.balderdash
  private lazy val atMyself: String = new ComponentAt(BotConfig.qID).toString

  /**
   * 监听消息
   *
   * @param eventMessage 消息对象
   */
  @EventHandler
  def listen(eventMessage: EventMessage): Unit = {
    if (eventMessage.isInstanceOf[EventPrivateMessage] || eventMessage.message.contains(atMyself)) {
      balderdash.size pipe Random.nextInt pipe balderdash pipe eventMessage.respond
    }
  }

}
