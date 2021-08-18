package o.lartifa.jam.cool.qq.listener.interactive

import akka.actor.ActorRef
import o.lartifa.jam.cool.qq.listener.interactive.Interactive.InteractiveFunction
import o.lartifa.jam.model.SpecificSender

/**
 * 交互式会话协议
 *
 * Author: sinar
 * 2021/8/18 23:23
 */
object InteractiveSessionProtocol {

  object Manage {
    sealed trait Request
    case class Register(msgSender: SpecificSender, f: InteractiveFunction, senderRef: ActorRef) extends Request
    case class Unregister(msgSender: SpecificSender, senderRef: ActorRef) extends Request
    case class Search(msgSender: SpecificSender, senderRef: ActorRef) extends Request
    sealed trait Response
    case class Registered(ref: ActorRef) extends Response
    case class Unregistered(refOpt: Option[ActorRef]) extends Response
    case class Found(ref: ActorRef) extends Response
    case object NotFound extends Response
  }

}
