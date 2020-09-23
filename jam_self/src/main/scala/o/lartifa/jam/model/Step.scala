package o.lartifa.jam.model

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 步骤对象
 *
 * Author: sinar
 * 2020/1/4 01:29 
 */
case class Step(id: Long, executable: Executable[_]) extends Executable[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    await(executable.execute())
  }
}
