package o.lartifa.jam.model.structure

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
 * 随机 Or 结构
 * Author: sinar
 * 2020/1/4 19:23 
 */
case class RandomOr(commands: List[Command[_]]) extends Command[Boolean] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Boolean] = {
    Random.shuffle(commands).head.execute().map(_ => true)
  }
}
