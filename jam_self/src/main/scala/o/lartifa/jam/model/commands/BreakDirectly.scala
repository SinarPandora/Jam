package o.lartifa.jam.model.commands

import o.lartifa.jam.cool.qq.listener.base.ExitCodes
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 打断当前执行
 *
 * Author: sinar
 * 2021/6/13 18:29
 */
object BreakDirectly extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] =
    break(ExitCodes.Break)
}
