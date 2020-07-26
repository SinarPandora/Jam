package o.lartifa.jam.plugins.picbot

import cc.moecraft.icq.sender.message.components.ComponentImageBase64
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.database.temporary.TemporaryMemory.database.db
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.pool.JamContext

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 获取并发送图片指令
 *
 * Author: sinar
 * 2020/7/12 12:12
 */
case class FetchAndSendPic(amount: Int) extends Command[Unit] {
  /*
  * TODO：
  *  1. 多次请求顺序执行
  *  2. 图片获取异步执行
  *  3. 速度优化 ✔️
  * */

  import o.lartifa.jam.database.temporary.TemporaryMemory.database.profile.api._

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
    val enableR18 = await(CONFIG_ALLOWED_R18.queryOrElseUpdate("false")).toBoolean
    val isOnly = await(CONFIG_MODE.queryOrElseUpdate(RANGE.str)) == ONLY.str
    val result = if (enableR18 && isOnly) {
      await(db.run(WebPictures.filter(_.isR18 === true).drop(lastId).take(amount).result))
    } else if (enableR18 && !isOnly) {
      await(db.run(WebPictures.drop(lastId).take(amount).result))
    } else {
      await(db.run(WebPictures.filter(_.isR18 === false).drop(lastId).take(amount).result))
    }

    result.headOption match {
      case Some(record) =>
        record.base64Data match {
          case Some(data) => context.eventMessage.respond(new ComponentImageBase64(data).toString)
          case None => context.eventMessage.respond("咦，这张色图好像被吃掉了？")
        }
        await(CONFIG_ID.update((lastId + amount).toString))
      case None => context.eventMessage.respond("色图用光光啦~")
    }

  }
}
