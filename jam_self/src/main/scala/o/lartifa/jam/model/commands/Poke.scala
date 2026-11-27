package o.lartifa.jam.model.commands

import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.onebot.cq.Cq

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * 真·戳一戳指令
 *
 * Author: sinar
 * 2020/12/27 02:03
 */
case class Poke(qId: RenderStrTemplate) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = Future {
    qId.execute().foreach { qId =>
      reply(Cq.Poke(Try(qId.toLong).getOrElse(throw ExecutionException("群员QQ号不正确"))))
    }
  }
}
