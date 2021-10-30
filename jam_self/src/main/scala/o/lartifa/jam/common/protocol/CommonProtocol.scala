package o.lartifa.jam.common.protocol

import akka.actor.ActorRef

/**
 * 公共返回协议
 *
 * Author: sinar
 * 2021/8/22 01:53
 */
sealed trait Response
case object Done extends Response
case class Fail(msg: String) extends Response
case class Data[T](data: T) extends Response
case class Retry(time: Int) extends Response
case object Online extends Response
case object Offline extends Response
sealed trait Request
case class IsAlive(senderRef: ActorRef) extends Request
case class Exit(senderRef: ActorRef) extends Response
