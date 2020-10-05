package o.lartifa.jam.cool.qq.listener

import java.util.concurrent.TimeUnit

import cc.moecraft.icq.event.events.request.{EventFriendRequest, EventGroupAddRequest, EventGroupInviteRequest, EventRequest}
import cc.moecraft.icq.event.{EventHandler, IcqListener}
import io.reactivex.rxjava3.core.Observable
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
    lazyRefresh(event)
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
    lazyRefresh(event)
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
    lazyRefresh(event)
  }

  /**
   * 五秒后刷新基本信息
   *
   * @param event 事件对象
   */
  private def lazyRefresh(event: EventRequest): Unit = {
    Observable.empty().delay(5, TimeUnit.SECONDS).doOnComplete{ () =>
      event.getBot.getAccountManager.refreshCache()
    }.subscribe()
  }
}
