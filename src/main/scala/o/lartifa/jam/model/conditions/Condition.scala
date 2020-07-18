package o.lartifa.jam.model.conditions

import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 情况原型
 *
 * Author: sinar
 * 2020/1/4 16:18
 */
abstract class Condition {
  /**
   * 是否匹配该种情况
   *
   * @param context 指令执行上下文
   * @param exec    异步执行上下文
   * @return 匹配结果
   */
  def isMatched(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Boolean]
}
