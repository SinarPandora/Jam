package o.lartifa.jam.plugins.picbot

import upickle.default.*

/**
 * 图片 API 返回体
 *
 * Author: sinar
 * 2020/11/19 00:37
 */
object APIResponse {

  case class Response(error: String, data: List[PicData])

  case class PicData
  (
    pid: Long,
    uid: Long,
    title: String,
    author: String,
    r18: Boolean,
    width: Int,
    height: Int,
    tags: List[String],
    urls: Map[String, String]
  )

  implicit val responseRw: ReadWriter[Response] = macroRW
  implicit val picDataRw: ReadWriter[PicData] = macroRW

}
