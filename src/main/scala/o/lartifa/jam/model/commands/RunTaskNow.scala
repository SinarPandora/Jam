package o.lartifa.jam.model.commands

import java.util.concurrent.atomic.AtomicBoolean

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.tasks.JamCronTask

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 立即执行任务指令
 *
 * Author: sinar
 * 2020/7/25 20:05
 */
case class RunTaskNow(task: JamCronTask, isSingleton: Boolean) extends Command[Unit] {
  val isRunning: AtomicBoolean = new AtomicBoolean(false)
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    if (isRunning.get() && isSingleton) {
      context.eventMessage.respond(s"该任务正在运行，且同时只能运行一个（任务名：${task.name}）")
    } else {
      isRunning.set(true)
      await(task.run())
      isRunning.set(false)
    }
  }
}
