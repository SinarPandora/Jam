package o.lartifa.jam.model.commands

import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 跳转执行指令
 *
 * Author: sinar
 * 2020/1/4 15:59 
 */
case class GoTo(stepId: Long) extends Command[Unit] {
  /**
   * 执行指令
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = {
    val pool = context.stepPool
    pool.goto(stepId)
  }
}
