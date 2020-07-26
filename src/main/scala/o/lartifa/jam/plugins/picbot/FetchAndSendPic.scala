package o.lartifa.jam.plugins.picbot

import cc.moecraft.icq.sender.message.components.ComponentImageBase64
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.pool.JamContext

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * 获取并发送图片指令
 *
 * Author: sinar
 * 2020/7/12 12:12
 */
case class FetchAndSendPic(amount: Int) extends Command[Unit] {

  private lazy val logger: HyLogger = JamContext.logger.get()

  /**
   * 获取一张图片并发送
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    val lastId = await(CONFIG_ID.queryOrElseUpdate("0")).toInt
    val result = await(PictureUtil.getPictureById(lastId))

    result.headOption match {
      case Some(record) =>
        record.base64Data match {
          case Some(data) => Try(context.eventMessage.respond(new ComponentImageBase64(data).toString))
            .getOrElse(context.eventMessage.respond("咦，发送失败了，是不是图片太大了。。"))
          case None => context.eventMessage.respond("咦，这张色图好像被吃掉了？")
        }
        await(CONFIG_ID.update((lastId + amount).toString))
      case None => context.eventMessage.respond("色图用光光啦~")
    }

  }
}
