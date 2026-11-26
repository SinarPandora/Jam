package o.lartifa.jam.common.util

import o.lartifa.jam.ob.PicqBotX
import o.lartifa.jam.ob.accounts.AccountManagerListener
import o.lartifa.jam.ob.event.IcqListener
import o.lartifa.jam.ob.listeners.HyExpressionListener
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.cool.qq.listener.{QEventListener, QMessageListener, SleepingStateListener, SystemEventListener}
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
      bot.getEventManager.registerListeners(recreateSystemListeners()*)
      bot.getEventManager.registerListener(SystemEventListener)
      if (JamConfig.config.balderdash.nonEmpty) {
        bot.getEventManager.registerListener(SleepingStateListener)
      }
    }

    /**
     * 切换为正常模式
     */
    def switchToWakeUpMode(): Unit = {
      this.deregisterAllListeners()
      bot.getEventManager.registerListeners(recreateSystemListeners()*)
      bot.getEventManager.registerListeners(QMessageListener, QEventListener, SystemEventListener)
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
