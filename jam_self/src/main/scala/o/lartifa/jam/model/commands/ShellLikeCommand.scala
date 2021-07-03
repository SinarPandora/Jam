package o.lartifa.jam.model.commands

import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 需实时处理参数的指令（复杂指令）
 *
 * Author: sinar
 * 2021/7/3 19:24
 */
abstract class ShellLikeCommand(prefixes: List[String]) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = {
    val msg = context.eventMessage.message
    val content = prefixes.find(msg.startsWith).map(msg.stripPrefix).getOrElse(msg)
    content.split("\\W+").toList match {
      case "help" :: Nil => help()
      case "帮助" :: Nil => help()
      case name :: args => this.execute(name, args)
      case Nil => throw ExecutionException(s"捕获到的指令没有内容，请不要将指令名本身放在前缀中，指令前缀：${prefixes.mkString(",")},消息内容：$msg")
    }
  }

  /**
   * 执行
   *
   * @param name    指令名称
   * @param args    指令参数
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  def execute(name: String, args: List[String])(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit]

  /**
   * 输出指令帮助信息
   * 若帮助信息过长，请手动分隔发送
   *
   * @return 帮助信息
   */
  def help()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit]
}
