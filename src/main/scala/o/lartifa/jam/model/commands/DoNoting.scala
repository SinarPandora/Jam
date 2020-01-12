package o.lartifa.jam.model.commands

import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 什么都，不做
 *
 * Author: sinar
 * 2020/1/4 16:07 
 */
object DoNoting extends Command[Boolean] {
  /**
   * 执行指令
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Boolean] = Future.successful(true)
}
