package o.lartifa.jam.model.commands

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.commands.IgnoreError.logger
import o.lartifa.jam.model.{CommandExecuteContext, Executable}
import o.lartifa.jam.pool.JamContext

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 忽略错误
 * 只会忽略预期的异常（ExecutionException）
 *
 * Author: sinar
 * 2020/11/1 12:05
 */
case class IgnoreError(command: Executable[_]) extends Command[Any] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Any] = async {
    try {
      await(command.execute())
    } catch {
      case e: ExecutionException => logger.error(e)
        "出现错误"
      case e => throw e
    }
  }
}

object IgnoreError {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(IgnoreError.getClass)
}
