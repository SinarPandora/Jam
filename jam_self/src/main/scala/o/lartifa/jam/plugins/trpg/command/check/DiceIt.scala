package o.lartifa.jam.plugins.trpg.command.check

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.ShellLikeCommand

import scala.concurrent.{ExecutionContext, Future}

/**
 * 投掷指令
 *
 * Author: sinar
 * 2021/7/15 23:00
 */
case class DiceIt(prefixes: Set[String]) extends ShellLikeCommand(prefixes) {
  /**
   * 执行
   *
   * @param args    指令参数
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute(args: List[String])(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = ???

  /**
   * 输出指令帮助信息
   * 若帮助信息过长，请手动分隔发送
   *
   * @return 帮助信息
   */
  override def help()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = Future {
    reply(
      """投掷指令（全功能）
        |""".stripMargin)
  }
}
