package o.lartifa.jam.cool.qq.listener.event

import o.lartifa.jam.model.ChatInfo

/**
 * 酷Q事件的果酱封装
 *
 * Author: sinar
 * 2021/7/7 00:09
 */
abstract class CQEvent(val name: String, val senderId: Long) extends Extractable {
  /**
   * 会话信息
   */
  val chatInfo: ChatInfo

  /**
   * 该事件是否将不会被响应
   */
  val willNotResponse: Boolean = false
}
