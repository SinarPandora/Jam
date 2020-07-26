package o.lartifa.jam.cool.qq.listener

import cc.moecraft.icq.event.events.request.{EventFriendRequest, EventGroupAddRequest}
import cc.moecraft.icq.event.{EventHandler, IcqListener}
import o.lartifa.jam.common.config.JamConfig

/**
 * 系统事件监听器
 *
 * Author: sinar
 * 2020/1/18 02:59
 */
object SystemEventListener extends IcqListener {

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
}
