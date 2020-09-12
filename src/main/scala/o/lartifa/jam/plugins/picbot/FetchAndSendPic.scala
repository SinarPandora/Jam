package o.lartifa.jam.plugins.picbot

import cc.moecraft.icq.sender.message.components.ComponentImageBase64
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.database.temporary.schema.Tables
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.plugins.picbot.FetchAndSendPic.logger
import o.lartifa.jam.pool.JamContext

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

/**
 * 获取并发送图片指令
 *
 * Author: sinar
 * 2020/7/12 12:12
 */
case class FetchAndSendPic(amount: Int) extends Command[Unit] {

  /**
   * 获取一张图片并发送
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    val lastId = await(CONFIG_ID.queryOrElseUpdate("0")).toInt
    val result = await(PictureUtil.getPictureById(lastId, amount))

    result match {
      case Nil =>
        context.eventMessage.respond("色图用光光啦~")
        val name = await(context.tempVars.get("群昵称")).getOrElse("我也不知道是谁")
        MasterUtil.notifyMaster(s"%s，${name}已经把色图用光了，太强了。。（${context.eventMessage.getSenderId}）")
      case head :: Nil =>
        await(CONFIG_ID.update((lastId + 1).toString))
        sendPic(head)
      case list =>
        await(CONFIG_ID.update((lastId + amount).toString))
        list.foreach { record =>
          sendPic(record)
          // 等待一秒以防被屏蔽
          Thread.sleep(1000)
        }
    }
  }

  /**
   * 发送图片
   *
   * @param record  图片信息
   * @param context 执行上下文
   */
  def sendPic(record: Tables.WebPicturesRow)(implicit context: CommandExecuteContext): Unit = {
    record.base64Data match {
      case Some(data) => Try(context.eventMessage.respond(new ComponentImageBase64(data).toString))
        .recover(e => {
          logger.warning("由于网络原因或图片大小原因发送可能失败。如果图片可以正常显示，请忽略该错误。")
          logger.error(e)
          Failure(e)
        })
      case None => context.eventMessage.respond("咦，这张色图好像被吃掉了？")
    }
  }
}


object FetchAndSendPic {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(FetchAndSendPic.getClass)
}
