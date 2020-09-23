package o.lartifa.jam.model.commands
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * 循环执行指令
 *
 * Author: sinar
 * 2020/7/23 20:37
 */
case class LoopGoto(stepIds: List[Long], inOrder: Boolean, times: Int) extends Command[Unit] {
  protected val run: OneByOne = OneByOne(stepIds, inOrder)
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = Future {
    for (_ <- 1 to times) {
      Await.result(run.execute(), Duration.Inf)
    }
  }
}

object LoopGoto {
  sealed abstract class Mode(val str: String, val value: Boolean)
  case object InOrder extends Mode("循环顺序", true)
  case object NotInOrder extends Mode("循环", false)
}
