package o.lartifa.jam.plugins.picbot

import upickle.default._

/**
 * 图片 API 返回体
 *
 * Author: sinar
 * 2020/11/19 00:37
 */
object APIResponse {

  case class Response(code: Int, msg: String, count: Int, data: List[PicData])

  case class PicData
  (
    pid: Long,
    uid: Long,
    title: String,
    author: String,
    url: String,
    r18: Boolean,
    width: Int,
    height: Int,
    tags: List[String]
  )

  implicit val responseRw: ReadWriter[Response] = macroRW
  implicit val picDataRw: ReadWriter[PicData] = macroRW

}
