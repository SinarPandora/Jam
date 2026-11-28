package o.lartifa.jam.cool.qq.listener

import o.lartifa.jam.ob.event.events.meta.EventMetaHeartbeat
import o.lartifa.jam.ob.event.{EventHandler, IcqListener}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.onebot.domain.{FriendAddRequest, GroupInviteRequest, RequestEvent}
import o.lartifa.jam.pool.JamContext

import scala.util.Try

/**
 * 系统事件监听器：request 由 OneBotInbound 直喂；meta 仍挂 EventManager。
 *
 * Author: sinar
 * 2020/1/18 02:59
 */
object SystemEventListener extends IcqListener {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(SystemEventListener.getClass)

  /**
   * 处理热路径请求事件（好友申请 / 群邀请）
   */
  def handleRequestEvent(event: RequestEvent): Unit = {
    event match {
      case _: GroupInviteRequest =>
        if (JamConfig.config.autoAcceptGroupRequest) {
          event.accept()
        }
      case _: FriendAddRequest =>
        if (JamConfig.config.autoAcceptFriendRequest) {
          event.accept()
        }
    }
    Try(JamContext.bot.get().getAccountManager.refreshCache()).failed.foreach { err =>
      logger.debug(s"请求处理后刷新缓存失败：${err.getMessage}")
    }
  }

  /**
   * 心跳事件刷新缓存
   *
   * @param event 心跳事件
   */
  @EventHandler
  def autoRefresh(event: EventMetaHeartbeat): Unit = {
    logger.debug("正在刷新账号缓存")
    Try(event.getBot.getAccountManager.refreshCache()).foreach(_ => {
      logger.debug("缓存刷新成功")
    })
  }
}
