package o.lartifa.jam.model.commands

import cc.moecraft.icq.sender.message.components.ComponentDice
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * QQ 动画骰子指令
 *
 * Author: sinar
 * 2020/12/27 02:04
 */
object QQDice extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = Future {
    reply(new ComponentDice())
  }
}
