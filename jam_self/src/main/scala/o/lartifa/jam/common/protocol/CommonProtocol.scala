package o.lartifa.jam.common.protocol

import akka.actor.ActorRef

/**
 * 公共协议
 * * TODO 并不是很好的设计，
 * *  某些使用公共协议，某些不使用，
 * *  导致模式匹配时无法得知目标 Actor 具体能返回什么样的消息
 * *  如果一定要设置公共协议，那就必须让每个 Actor 都要能够处理/返回这里的消息
 *
 * Author: sinar
 * 2021/8/22 01:53
 */
object CommonProtocol {
  // 请求
  sealed trait Request
  // 健康检查
  case class IsAlive_?(fromRef: ActorRef) extends Request
  // 请求退出
  case class Exit(fromRef: ActorRef) extends Response
  // 响应
  sealed trait Response
  // 操作完毕
  case object Done extends Response
  // 操作失败
  case class Fail(msg: String) extends Response
  // 返回数据
  case class Data[T](data: T) extends Response
  // 已重试
  case class Retry(time: Int) extends Response
  // 在线
  case object Online extends Response
  // 离线
  case object Offline extends Response
}
