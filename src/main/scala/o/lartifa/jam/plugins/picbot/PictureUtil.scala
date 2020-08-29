package o.lartifa.jam.plugins.picbot

import o.lartifa.jam.database.temporary.TemporaryMemory.database.db
import o.lartifa.jam.database.temporary.schema.Tables
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.model.CommandExecuteContext

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * Author: sinar
 * 2020/7/26 12:57
 */
object PictureUtil {

  import o.lartifa.jam.database.temporary.TemporaryMemory.database.profile.api._

  /**
   * 从数据库获取图片
   *
   * @param id      图片位置 ID
   * @param amount  图片数量
   * @param context 指令执行上下文
   * @param exec    异步执行上下文
   * @return 图片列表
   */
  def getPictureById(id: Int, amount: Int = 1)(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[List[Tables.WebPicturesRow]] = async {
    val (enableR18, isOnly) = await {
      CONFIG_ALLOWED_R18.queryOrElseUpdate("false").map(_.toBoolean) zip
        CONFIG_MODE.queryOrElseUpdate(RANGE.str).map(_ == ONLY.str)
    }

    val result = if (enableR18 && isOnly) {
      await(db.run(WebPictures.filter(_.isR18 === true).drop(id).take(amount).result))
    } else if (enableR18 && !isOnly) {
      await(db.run(WebPictures.drop(id).take(amount).result))
    } else {
      await(db.run(WebPictures.filter(_.isR18 === false).drop(id).take(amount).result))
    }
    result.toList
  }
}
