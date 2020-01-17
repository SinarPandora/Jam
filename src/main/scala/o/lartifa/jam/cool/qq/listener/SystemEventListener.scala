package o.lartifa.jam.cool.qq.listener

import cc.moecraft.icq.event.events.request.EventGroupAddRequest
import cc.moecraft.icq.event.{EventHandler, IcqListener}

/**
 * 系统事件监听器
 *
 * Author: sinar
 * 2020/1/18 02:59 
 */
object SystemEventListener extends IcqListener {

  /**
   * 加群时自动刷新系统缓存
   *
   * @param event 加群事件
   */
  @EventHandler
  def refreshCacheWhenAddToNewGroup(event: EventGroupAddRequest): Unit = event.getBot.getAccountManager.refreshCache()
}
