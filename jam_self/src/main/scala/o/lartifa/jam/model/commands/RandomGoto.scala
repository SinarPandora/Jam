package o.lartifa.jam.model.commands

import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
 * 随机执行指令
 *
 * Author: sinar
 * 2020/1/4 17:05
 */
case class RandomGoto(stepIds: List[Long], amount: Int) extends Command[Unit] {
  /**
   * 执行指令
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = {
    OneByOne(List.from(Random.shuffle(stepIds)).take(amount), inOrder = false).execute()
  }
}
