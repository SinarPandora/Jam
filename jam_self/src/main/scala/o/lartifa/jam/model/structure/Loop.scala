package o.lartifa.jam.model.structure

import o.lartifa.jam.model.{CommandExecuteContext, Executable}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * 循环结构
 *
 * Author: sinar
 * 2020/1/4 21:47 
 */
case class Loop(executable: Executable[_], times: Int) extends LogicStructure {

  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Boolean] = {
    LazyList.from(Range(0, times)).foreach(_ => Await.result(executable.execute(), Duration.Inf))
    Future.successful(true)
  }
}
