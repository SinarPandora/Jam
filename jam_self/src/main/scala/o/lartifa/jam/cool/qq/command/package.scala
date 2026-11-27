package o.lartifa.jam.cool.qq

import o.lartifa.jam.ob.event.events.message.EventMessage
import o.lartifa.jam.onebot.LegacyCommandBridge
import o.lartifa.jam.onebot.domain.ChatMessage

import scala.language.implicitConversions

/**
 * 管理指令仍收 legacy EventMessage，调用已迁移 API 时临时转成 ChatMessage。
 * 后续「指令」刀删除。
 */
package object command {
  implicit def legacyEventToChatMessage(event: EventMessage): ChatMessage = LegacyCommandBridge.fromLegacyEvent(event)
}
