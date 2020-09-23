package o.lartifa.jam.cool.qq.command.base

import java.util

import cc.moecraft.icq.command.CommandProperties
import cc.moecraft.icq.command.interfaces.EverywhereCommand
import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.user.User
import o.lartifa.jam.common.config.JamConfig

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * 监管者任意消息指令原型
 *
 * Author: sinar
 * 2020/1/5 11:29 
 */
abstract class MasterEverywhereCommand(prefix: String*)(implicit exec: ExecutionContext) extends EverywhereCommand with MasterCommand {

  override final def run(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): String = {
    if (sender.getId == JamConfig.masterQID) {
      Await.result(task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]), Duration.Inf)
    } else NO_RESPONSE
  }

  override final def properties(): CommandProperties = createProperties(prefix)

  /**
   * 指令操作
   *
   * @param event   消息事件
   * @param sender  发送者
   * @param command 指令内容
   * @param args    参数
   * @return 输出内容
   */
  def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String]
}
