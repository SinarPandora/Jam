package o.lartifa.jam.cool.qq.listener

import cc.moecraft.icq.event.events.meta.EventMetaHeartbeat
import cc.moecraft.icq.event.events.request.{EventFriendRequest, EventGroupAddRequest, EventGroupInviteRequest}
import cc.moecraft.icq.event.{EventHandler, IcqListener}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.pool.JamContext

import scala.util.Try

/**
 * 系统事件监听器
 *
 * Author: sinar
 * 2020/1/18 02:59
 */
object SystemEventListener extends IcqListener {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(SystemEventListener.getClass)

  /**
   * 自动加群
   *
   * @param event 加群事件
   */
  @EventHandler
  def autoAddGroup(event: EventGroupAddRequest): Unit = {
    if (JamConfig.autoAcceptGroupRequest) {
      event.accept()
    }
    event.getBot.getAccountManager.refreshCache()
  }

  /**
   * 自动加群
   *
   * @param event 加群事件
   */
  @EventHandler
  def autoAddGroupAlt(event: EventGroupInviteRequest): Unit = {
    if (JamConfig.autoAcceptGroupRequest) {
      event.accept()
    }
    event.getBot.getAccountManager.refreshCache()
  }

  /**
   * 自动加好友
   *
   * @param event 加好友事件
   */
  @EventHandler
  def autoAddFriends(event: EventFriendRequest): Unit = {
    if (JamConfig.autoAcceptFriendRequest) {
      event.accept()
    }
    event.getBot.getAccountManager.refreshCache()
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
