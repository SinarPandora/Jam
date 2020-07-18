package o.lartifa.jam.model

import o.lartifa.jam.model.behaviors.StringAsVarKey

import scala.concurrent.{ExecutionContext, Future}

/**
 * Author: sinar
 * 2020/1/4 18:59
 */
trait Executable[T] extends StringAsVarKey {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[T]
}
