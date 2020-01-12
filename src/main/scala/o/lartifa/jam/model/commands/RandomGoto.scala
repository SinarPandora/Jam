package o.lartifa.jam.model.commands

import o.lartifa.jam.common.exception.ExecuteException
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
 * 随机执行指令
 *
 * Author: sinar
 * 2020/1/4 17:05 
 */
case class RandomGoto(stepIds: List[Long], amount: Int) extends Command[List[Future[Unit]]] {
  /**
   * 执行指令
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[List[Future[Unit]]] = Future {
    val pool = context.stepPool
    LazyList.from(Random.shuffle(stepIds))
      .take(amount)
      .map(id => (id, pool.get(id)))
      .tapEach(pair => if (pair._2.isEmpty) throw ExecuteException(s"指定步骤${pair._1}不存在"))
      .flatMap(_._2)
      .map(_.execute())
      .toList
  }
}
