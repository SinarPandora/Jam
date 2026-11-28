package o.lartifa.jam.common.util

import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.cool.qq.listener.SleepingStateListener
import o.lartifa.jam.onebot.OneBotInbound

/**
 * 清醒 / 睡眠切换（不再碰 EventManager）。
 *
 * Author: sinar
 * 2026/11/28
 */
object BotMode {

  def switchToSleepMode(): Unit =
    if (JamConfig.config.balderdash.nonEmpty) {
      OneBotInbound.setMessageHandler(SleepingStateListener.listen)
    } else {
      OneBotInbound.setMessageHandler(_ => ())
    }

  def switchToWakeUpMode(): Unit =
    OneBotInbound.resetMessageHandler()
}
