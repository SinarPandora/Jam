package o.lartifa.jam.model.commands

import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 等待
 *
 * Author: sinar
 * 2020/1/5 14:29 
 */
case class Waiting(sec: Int) extends Command[Boolean] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Boolean] = {
    Thread.sleep(sec * 1000)
    Future.successful(true)
  }
}
