package o.lartifa.jam.plugins.trpg.command.manage

import o.lartifa.jam.cool.qq.listener.interactive.Interactive
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.behaviors.ActorCreator
import o.lartifa.jam.model.commands.ShellLikeCommand

import scala.concurrent.{ExecutionContext, Future}

/**
 * Author: sinar
 * 2021/9/11 22:32
 */
case class ListGame(prefixes: Set[String]) extends ShellLikeCommand(prefixes) with Interactive with ActorCreator {
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
  override def help()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = ???
}
