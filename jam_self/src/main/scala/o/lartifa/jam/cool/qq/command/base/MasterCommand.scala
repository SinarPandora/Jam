package o.lartifa.jam.cool.qq.command.base

import o.lartifa.jam.onebot.domain.ChatMessage

import scala.util.{Failure, Success, Try}

/**
 * 监管者指令辅助工具
 *
 * Author: sinar
 * 2020/1/5 12:04
 */
trait MasterCommand {

  /** 不回复内容 */
  val NO_RESPONSE: String = ""

  /**
   * 将提供的参数都转换为编号列表
   *
   * @param chat 消息对象
   * @param args 指令参数
   * @param name id 名称
   * @return 编号列表
   */
  def argsToIds(chat: ChatMessage, args: Seq[String], name: String = ""): Seq[Int] = {
    val ids = args.flatMap { it =>
      Try(Integer.parseInt(it)) match {
        case Failure(_) =>
          chat.respond(s"提供的${name}编号有误：$it")
          None
        case Success(id) => Some(id)
      }
    }
    if (ids.isEmpty) {
      chat.respond(s"请提供至少一个有效的${name}编号")
    }
    ids
  }
}
