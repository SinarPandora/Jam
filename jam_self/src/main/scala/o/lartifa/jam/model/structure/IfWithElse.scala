package o.lartifa.jam.model.structure

import o.lartifa.jam.model.{CommandExecuteContext, Executable}

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * If-Else 结构
 *
 * Author: sinar
 * 2020/1/4 19:10 
 */
case class IfWithElse(`if`: If, orElse: Executable[_]) extends LogicStructure {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Boolean] = async {
    // 当 IF 没有执行时，执行 Else
    if (!await(`if`.execute())) {
      await(orElse.execute())
      true
    } else false
  }
}
