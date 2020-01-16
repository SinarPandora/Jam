package o.lartifa.jam.model.commands

import o.lartifa.jam.model.CommandExecuteContext

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 依次执行指令
 *
 * Author: sinar
 * 2020/1/16 23:59 
 */
case class OneByOne(stepIds: List[Long]) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    val pool = context.stepPool
    for (id <- stepIds) {
      await(pool.goto(id))
    }
  }
}
