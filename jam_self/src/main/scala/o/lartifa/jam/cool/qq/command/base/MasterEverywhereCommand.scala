package o.lartifa.jam.cool.qq.command.base

import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.onebot.domain.ChatMessage

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * 监管者任意消息指令原型（领域 ChatMessage，不再绑 Picq EverywhereCommand）
 *
 * Author: sinar
 * 2020/1/5 11:29
 */
abstract class MasterEverywhereCommand(nameParts: String*)(implicit exec: ExecutionContext)
  extends MasterCommand {

  /** 指令名 + 别名（注册用） */
  val names: Seq[String] = nameParts.toSeq

  final def run(chat: ChatMessage, command: String, args: List[String]): String = {
    if (JamConfig.config.forMaster.masterList.contains(chat.senderId)) {
      Await.result(task(chat, command, args), Duration.Inf)
    } else NO_RESPONSE
  }

  /**
   * 指令操作
   *
   * @param chat    消息
   * @param command 命中的指令名（小写）
   * @param args    参数
   * @return 输出内容（非空时由调度器 respond）
   */
  def task(chat: ChatMessage, command: String, args: List[String]): Future[String]
}
