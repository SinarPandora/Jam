package o.lartifa.jam.model.commands

import cc.moecraft.icq.sender.message.components.ComponentShare
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 分享链接
 *
 * Author: sinar
 * 2020/12/27 02:03
 */
case class ShareURL(url: RenderStrTemplate, title: RenderStrTemplate, content: RenderStrTemplate, image: RenderStrTemplate) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] =
    Future.sequence(Seq(url, title, content, image).map(_.execute())).map(params => {
      val Seq(url, title, content, image) = params
      reply(new ComponentShare(url, title, content, image))
    })

}
