package o.lartifa.jam.model.commands

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.tasks.JamCronTask
import o.lartifa.jam.pool.CronTaskPool.TaskDefinition

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 立即执行任务指令
 *
 * Author: sinar
 * 2020/7/25 20:05
 */
case class RunTaskNow(task: Either[JamCronTask, TaskDefinition]) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    task match {
      case Left(task) =>
        if (task.isRunning.get()) {
          reply(s"${task.name}已在运行中...")
        } else {
          reply(s"${task.name}已启动！")
          task.execute()
        }
      case Right(definition) =>
        reply(s"${definition.name}已启动！")
        definition.init().execute()
    }
  }
}
