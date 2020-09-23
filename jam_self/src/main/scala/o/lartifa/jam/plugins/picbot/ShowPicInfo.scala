package o.lartifa.jam.plugins.picbot

import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 查看图片信息指令
 *
 * Author: sinar
 * 2020/7/26 12:24
 */
case class ShowPicInfo() extends Command[String] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[String] = async {
    await(CONFIG_ID.query) match {
      case Some(value) =>
        val posId = value.toInt - 1
        val picture = await(PictureUtil.getPictureById(posId))
          .headOption.getOrElse(throw ExecutionException(s"图片丢失！ID：$posId"))
        context.eventMessage.respond(
          s"""标题：${picture.title}
            |作者：${picture.author}
            |标签：${picture.tags.stripPrefix("[").stripSuffix("]")}
            |图片地址：${picture.url}
            |P站ID：${picture.pid}
            |作者ID：${picture.uid}
            |尺寸：${picture.width}x${picture.height}
            |由于 QQ 限制，只能查看最近一张图片的信息""".stripMargin
        )
        picture.pid.toString
      case None =>
        context.eventMessage.respond("你还没有查看过任何图片哦")
        "无"
    }
  }
}
