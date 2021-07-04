package o.lartifa.jam.common.util

import cc.moecraft.icq.PicqBotX
import cc.moecraft.icq.accounts.AccountManagerListener
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.listeners.HyExpressionListener
import o.lartifa.jam.common.config.JamCharacter
import o.lartifa.jam.cool.qq.listener.{QEventListener, SleepingStateListener, SystemEventListener}
import o.lartifa.jam.pool.JamContext

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
      bot.getEventManager.registerListeners(recreateSystemListeners(): _*)
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
      bot.getEventManager.registerListeners(recreateSystemListeners(): _*)
      bot.getEventManager.registerListeners(SystemEventListener, QEventListener)
    }
  }

  /**
   * 重建系统监听器
   *
   * @return 系统监听器
   */
  private def recreateSystemListeners(): List[IcqListener] = {
    val accountManager = JamContext.bot.get().getAccountManager
    List(new AccountManagerListener(accountManager), new HyExpressionListener())
  }
}
