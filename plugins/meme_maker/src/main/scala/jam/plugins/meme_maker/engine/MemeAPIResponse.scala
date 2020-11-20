package jam.plugins.meme_maker.engine

import upickle.default._

/**
 * Meme API 响应体
 *
 * Author: sinar
 * 2020/11/18 23:32
 */
object MemeAPIResponse {

  case class PicData(url: String)

  case class TemplateInfo
  (
    id: Long,
    name: String,
    status: String,
    fillings: String,
    date_created: String,
    example_gif: String,
    cover_img: String,
    make_count: Int,
    visit_count: Int,
    like_count: Int,
    user: String,
    is_new: Boolean,
    is_hot: Boolean,
  ) {
    /**
     * 获取模板槽（用于插入自定义内容）
     *
     * @return 模板信息
     */
    def templateSlots: List[String] = {
      val fillingInfo = read[Map[String, String]](fillings)
      getTemplateSlots(fillingInfo)
    }

    /**
     * 获取模板槽
     *
     * @param fillingInfo 模板信息
     * @param slotId      当前槽编号
     * @return 模板槽占位符列表
     */
    private def getTemplateSlots(fillingInfo: Map[String, String], slotId: Int = 0): List[String] = {
      fillingInfo.get(s"sentence$slotId") match {
        case Some(sentence) => sentence +: getTemplateSlots(fillingInfo, slotId + 1)
        case None => Nil
      }
    }
  }

  case class Response[T](ok: Boolean, body: T, msg: String)

  case class Template(id: Int, name: String, cover: String, uploader: String, like: Int)

  implicit val generateResponseRW: ReadWriter[Response[PicData]] = macroRW[Response[PicData]]
  implicit val infoResponseRW: ReadWriter[Response[TemplateInfo]] = macroRW[Response[TemplateInfo]]
  implicit val picDataRW: ReadWriter[PicData] = macroRW[PicData]
  implicit val templateInfoRW: ReadWriter[TemplateInfo] = macroRW[TemplateInfo]
}
