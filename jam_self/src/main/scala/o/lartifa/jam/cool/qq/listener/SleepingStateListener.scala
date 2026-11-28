package o.lartifa.jam.cool.qq.listener

import o.lartifa.jam.common.config.{BotConfig, JamConfig}
import o.lartifa.jam.onebot.cq.Cq
import o.lartifa.jam.onebot.domain.{ChatMessage, PrivateMessage}

import scala.util.Random
import scala.util.chaining.*

/**
 * 睡觉时被戳来戳去，给与反馈
 *
 * Author: sinar
 * 2020/9/19 10:45
 */
object SleepingStateListener {
  private def balderdash: List[String] = JamConfig.config.balderdash
  private lazy val atMyself: String = Cq.At(BotConfig.qID).toCq

  /**
   * 监听消息
   *
   * @param eventMessage 消息对象
   */
  def listen(eventMessage: ChatMessage): Unit = {
    if (eventMessage.isInstanceOf[PrivateMessage] || eventMessage.message.contains(atMyself)) {
      balderdash.size pipe Random.nextInt pipe balderdash pipe (eventMessage.reply(_))
    }
  }
}
