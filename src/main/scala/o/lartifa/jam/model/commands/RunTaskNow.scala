package o.lartifa.jam.model.commands

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.tasks.JamCronTask

import scala.concurrent.{ExecutionContext, Future}

/**
 * 立即执行任务指令
 *
 * Author: sinar
 * 2020/7/25 20:05
 */
case class RunTaskNow(task: JamCronTask) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = task.run()
}
