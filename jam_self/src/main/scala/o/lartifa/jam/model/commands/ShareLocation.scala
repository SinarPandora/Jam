package o.lartifa.jam.model.commands

import cc.moecraft.icq.sender.message.components.ComponentLocation
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 分享地理位置
 *
 * Author: sinar
 * 2020/12/27 02:35
 */
case class ShareLocation(lat: RenderStrTemplate, lon: RenderStrTemplate, title: RenderStrTemplate, content: RenderStrTemplate) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = {
    Future.sequence(Seq(lat.execute().map(_.toDouble), lon.execute().map(_.toDouble), title.execute(), content.execute()))
      .map(vars => {
        val Seq(_lat, _lon, _title, _content) = vars
        reply(new ComponentLocation(
          _lat.asInstanceOf[Double],
          _lon.asInstanceOf[Double],
          _title.asInstanceOf[String],
          _content.asInstanceOf[String]))
      }).recoverWith {
      case _: NumberFormatException => Future.failed(ExecutionException("经度或维度数值格式错误"))
      case otherError => Future.failed(otherError)
    }.map(_ => ())
  }
}
