package o.lartifa.jam.common.protocol

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
