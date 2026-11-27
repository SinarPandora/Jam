package o.lartifa.jam.model.commands

import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.onebot.cq.Cq

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * 分享音乐
 *
 * Author: sinar
 * 2020/12/27 14:20
 */
case class ShareMusic(mId: RenderStrTemplate, sourceType: Cq.MusicSource) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = {
    mId.execute().map(mId => {
      reply(Cq.Music(Try(mId.toInt).getOrElse(throw ExecutionException("分享音乐ID不合法")), sourceType))
    })
  }
}

object ShareMusic {

  sealed abstract class SourceType(val str: String)

  case object QQ extends SourceType("QQ")

  case object Netease extends SourceType("网易")

  case object XM extends SourceType("虾米")

}
