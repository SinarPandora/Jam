package o.lartifa.jam.model.structure

import o.lartifa.jam.model.conditions.Condition
import o.lartifa.jam.model.{CommandExecuteContext, Executable}

import scala.concurrent.{ExecutionContext, Future}

/**
 * If 结构
 *
 * Author: sinar
 * 2020/1/4 19:05
 */
case class If(condition: Condition, command: Executable[_]) extends LogicStructure {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Boolean] =
    condition.isMatched.flatMap {
      case true => command.execute().map(_ => true)
      case false => Future.successful(false)
    }
}
