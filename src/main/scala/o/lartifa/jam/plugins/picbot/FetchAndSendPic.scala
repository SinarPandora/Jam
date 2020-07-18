package o.lartifa.jam.plugins.picbot

import java.util.Base64

import ammonite.ops._
import cc.moecraft.icq.sender.message.components.ComponentImageBase64
import cc.moecraft.icq.sender.returndata.ReturnStatus
import cc.moecraft.logger.HyLogger
import com.jsoniter.JsonIterator
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.pool.JamContext

import scala.annotation.tailrec
import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

/**
 * 获取并发送图片指令
 *
 * Author: sinar
 * 2020/7/12 12:12
 */
case class FetchAndSendPic() extends Command[Unit] {
  /*
  * TODO：
  *  1. 多次请求顺序执行
  *  2. 图片获取异步执行
  *  3. 速度优化
  * */

  private lazy val logger: HyLogger = JamContext.logger.get()

  sealed case class Position(page: Int, item: Int) {
    override def toString: String = s"$page,$item"
  }

  sealed case class Picture(url: String, rating: String, oriUrl: String, position: Position) {
    def base64Img: ComponentImageBase64 =
      new ComponentImageBase64(Base64.getEncoder.encodeToString(requests.get(url).bytes))
  }

  /**
   * 获取一张图片并发送
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    val picPos = await(context.vars.getOrElseUpdate(CONFIG_PAGE, "0,0"))
    val rating = await(context.vars.getOrElseUpdate(CONFIG_ALLOWED_RATING, SAFE.str))
    val ratings = await(context.vars.getOrElseUpdate(CONFIG_MODE, ONLY.str)) match {
      case RANGE.str => ratingList(rating)
      case ONLY.str => Set(rating)
    }
    val page :: item :: Nil = picPos.split(",").take(2).map(_.toInt).toList
    val position = Position(page, item)
    Try(getImage(ratings, position)) match {
      case Failure(exception) =>
        logger.error(exception)
        context.eventMessage.respond("图片丢了（o´ﾟ□ﾟ`o）")
        MasterUtil.notifyMaster("[FetchAndSendPic] 图片获取出错")
      case Success(picture) =>
        context.vars.update(CONFIG_PAGE, picture.position.toString)
        Try(context.eventMessage.respond(picture.base64Img.toString)) match {
          case Failure(exception) => logger.warning(s"图片发送错误：${exception.getMessage}")
          case Success(response) =>
            if (response.getStatus == ReturnStatus.failed) {
              context.eventMessage.respond("图被企鹅吃掉了。。(つД｀)･ﾟ･")
            }
        }
    }
  }

  /**
   * 获取图片列表
   *
   * @param page 页数
   * @return 图片列表 json
   */
  private def fetchPicList(page: Int = 0): String = requests.get(API(page)).text()

  /**
   * 转换 json 为图片列表
   *
   * @param rawJson json 原始数据
   * @return 图片列表
   */
  private def parseJson(page: Int)(rawJson: String): List[Picture] = {
    JsonIterator.parse(rawJson).readAny()
      .asScala.toList
      .zipWithIndex
      .map {
        case (it, idx) =>
          Picture(
            url = it.toString("sample_url"),
            rating = it.toString("rating"),
            oriUrl = it.toString("file_url"),
            position = Position(page, idx)
          )
      }
  }

  /**
   * 获取一张满足条件的图片
   *
   * @param ratings 许可的图片等级列表
   * @param position 上一张图片的地址（坐标）
   * @return 图片信息
   */
  @tailrec
  private def getImage(ratings: Set[String], position: Position): Picture = {
    val list = position.page |> fetchPicList |> parseJson(position.page)
    list.drop(position.item).find(it => ratings.contains(it.rating)) match {
      case Some(picture: Picture) => picture.copy(position = position.copy(item = picture.position.item + 1))
      case None => getImage(ratings, position.copy(position.page + 1, 0))
    }
  }

  /**
   * 获取许可的图片等级集合
   *
   * @param rating 当前可允许的最大等级
   * @return 图标等级集合
   */
  private def ratingList(rating: String): Set[String] = {
    rating match {
      case SAFE.str => Set(SAFE.str)
      case QUESTIONABLE.str => Set(QUESTIONABLE.str, SAFE.str)
      case EXPLICIT.str => Set(EXPLICIT.str, QUESTIONABLE.str, SAFE.str)
    }
  }
}
