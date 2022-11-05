package o.lartifa.jam.model.commands

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.plugins.caiyunai.dream.DreamClient
import requests.Session

import scala.concurrent.{ExecutionContext, Future}
import scala.util.chaining.*

/**
 * 列出可用模型指令
 *
 * Author: sinar
 * 2021/6/19 21:44
 */
object ListAIModels extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = Future {
    implicit val session: Session = requests.Session()
    DreamClient.listModels match {
      case Left(_) => reply("获取失败请稍后重试")
      case Right(list) => list.zipWithIndex.map(it => s"${it._2}: ${it._1.name}").mkString("\n") pipe reply
    }
  }
}
