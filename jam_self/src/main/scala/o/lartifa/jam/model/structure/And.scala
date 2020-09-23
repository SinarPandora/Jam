package o.lartifa.jam.model.structure

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * And 结构
 * Author: sinar
 * 2020/1/4 19:15
 */
case class And(commands: List[Command[_]]) extends Command[Boolean] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Boolean] = Future {
    commands.foreach(command => Await.result(command.execute(), Duration.Inf))
    true
  }
}
