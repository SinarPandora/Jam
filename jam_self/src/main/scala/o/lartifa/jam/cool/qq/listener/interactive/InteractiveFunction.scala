package o.lartifa.jam.cool.qq.listener.interactive

import o.lartifa.jam.ob.event.events.message.EventMessage

/**
 * 交互函数
 *
 * Author: sinar
 * 2021/8/20 01:18
 */
trait InteractiveFunction {
  def apply(session: InteractiveSession, event: EventMessage): Unit
}
