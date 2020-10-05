package o.lartifa.jam.cool.qq.command.base

import java.util

import cc.moecraft.icq.command.CommandProperties
import cc.moecraft.icq.event.events.message.EventMessage

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

/**
 * 监管者指令 辅助工具
 *
 * Author: sinar
 * 2020/1/5 12:04
 */
trait MasterCommand {

  /**
   * 不回复内容
   */
  val NO_RESPONSE: String = ""

  /**
   * 创建指令捕获前缀属性
   *
   * @param prefix 前缀列表
   * @return 捕获属性
   */
  def createProperties(prefix: Seq[String]): CommandProperties = if (prefix.lengthIs > 1) {
    new CommandProperties(prefix.head, prefix.tail: _*)
  } else new CommandProperties(prefix.head)

  /**
   * 将提供的参数都转换为编号列表
   *
   * @param event 消息对象
   * @param args  指令参数
   * @param name  id 名称
   * @return 编号列表
   */
  def argsToIds(event: EventMessage, args: util.ArrayList[String], name: String = ""): Seq[Int] = {
    val ids = args.asScala.flatMap(it => {
      Try(Integer.parseInt(it)) match {
        case Failure(_) =>
          event.respond(s"提供的${name}编号有误：$it")
          None
        case Success(id) => Some(id)
      }
    }).toSeq
    if (ids.isEmpty) {
      event.respond(s"请提供至少一个有效的${name}编号")
    }
    ids
  }
}
