package o.lartifa.jam.model.commands

import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.{CommandExecuteContext, VarKey}

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

/**
 * 捕获匹配内容参数
 *
 * Author: sinar
 * 2020/7/25 10:47
 */
case class CatchParameters(regex: Regex, varKeys: Seq[(VarKey, Int)]) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    await {
      regex.findFirstMatchIn(context.eventMessage.getMessage).map(result => {
        Future.sequence(varKeys.map { case (key, idx) => key.updateOrElseSet(result.group(idx + 1)) })
      }).getOrElse(throw ExecutionException("没有匹配到捕获变量"))
    }
  }
}
