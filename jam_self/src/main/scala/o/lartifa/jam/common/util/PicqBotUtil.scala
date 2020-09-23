package o.lartifa.jam.common.util

import cc.moecraft.icq.PicqBotX
import o.lartifa.jam.common.config.JamCharacter
import o.lartifa.jam.cool.qq.listener.{EventMessageListener, SleepingStateListener, SystemEventListener}

/**
 * PicqBotX Bot 辅助工具
 *
 * Author: sinar
 * 2020/9/19 10:37
 */
object PicqBotUtil {

  implicit class Helper(bot: PicqBotX) {
    /**
     * 取消全部监听器
     */
    def deregisterAllListeners(): Unit = {
      bot.getEventManager.getRegisteredListeners.clear()
      bot.getEventManager.getRegisteredMethods.clear()
    }

    /**
     * 切换为睡眠模式
     */
    def switchToSleepMode(): Unit = {
      this.deregisterAllListeners()
      bot.getEventManager.registerListener(SystemEventListener)
      if (JamCharacter.balderdash.nonEmpty) {
        bot.getEventManager.registerListener(SleepingStateListener)
      }
    }

    /**
     * 切换为正常模式
     */
    def switchToWakeUpMode(): Unit = {
      this.deregisterAllListeners()
      bot.getEventManager.registerListeners(SystemEventListener, EventMessageListener)
    }
  }

}
