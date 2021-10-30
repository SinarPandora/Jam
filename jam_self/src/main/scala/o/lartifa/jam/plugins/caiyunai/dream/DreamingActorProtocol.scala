package o.lartifa.jam.plugins.caiyunai.dream

import akka.actor.ActorRef
import cc.moecraft.icq.event.events.message.EventMessage

/**
 * Dreaming Actor 协议
 *
 * Author: sinar
 * 2021/10/25 00:19
 */
object DreamingActorProtocol {
  sealed trait Message
  case class Login(senderRef: ActorRef, eventMessage: EventMessage) extends Message
  case class Reply(senderRef: ActorRef, content: String) extends Message
  case class Logout(senderRef: ActorRef) extends Message
  case object KeepAlive extends Message
}
