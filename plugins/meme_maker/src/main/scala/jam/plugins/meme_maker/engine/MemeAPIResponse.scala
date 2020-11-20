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
  )

  case class Response[T](ok: Boolean, body: T, msg: String)

  implicit val generateResponseRW: ReadWriter[Response[PicData]] = macroRW[Response[PicData]]
  implicit val infoResponseRW: ReadWriter[Response[TemplateInfo]] = macroRW[Response[TemplateInfo]]
  implicit val picDataRW: ReadWriter[PicData] = macroRW[PicData]
  implicit val templateInfoRW: ReadWriter[TemplateInfo] = macroRW[TemplateInfo]
}
