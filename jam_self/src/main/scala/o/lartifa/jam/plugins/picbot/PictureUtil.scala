package o.lartifa.jam.plugins.picbot

import o.lartifa.jam.database.Memory.database.db
import o.lartifa.jam.database.schema.Tables
import o.lartifa.jam.database.schema.Tables.*
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * Author: sinar
 * 2020/7/26 12:57
 */
object PictureUtil {

  import o.lartifa.jam.database.Memory.database.profile.api.*

  /**
   * 从数据库获取图片
   *
   * @param id      图片位置 ID
   * @param amount  图片数量
   * @param context 指令执行上下文
   * @param exec    异步执行上下文
   * @return 图片列表
   */
  def getPictureById(id: Int, amount: Int = 1)(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[List[Tables.WebPicturesRow]] = {
    val flagsF =
      CONFIG_ALLOWED_R18.queryOrElseUpdate("false").map(_.toBoolean) zip
        CONFIG_MODE.queryOrElseUpdate(RANGE.str).map(_ == ONLY.str)
    flagsF.flatMap { case (enableR18, isOnly) =>
      val query =
        if (enableR18 && isOnly) WebPictures.filter(_.isR18 === true).drop(id).take(amount).result
        else if (enableR18 && !isOnly) WebPictures.drop(id).take(amount).result
        else WebPictures.filter(_.isR18 === false).drop(id).take(amount).result
      db.run(query).map(_.toList)
    }
  }
}
