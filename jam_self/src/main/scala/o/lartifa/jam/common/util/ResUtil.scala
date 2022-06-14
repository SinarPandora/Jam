package o.lartifa.jam.common.util

import requests.Session

import java.util.Base64
import scala.util.Try

/**
 * 资源工具
 *
 * Author: sinar
 * 2022/6/14 22:54
 */
object ResUtil {
  val BASE64_IMAGE_EXTS: Map[String, String] = Map(
    "bmp" -> "bmp",
    "gif" -> "gif",
    "jpg" -> "jpeg",
    "jpeg" -> "jpeg",
    "png" -> "png",
    "tif" -> "tiff",
    "tiff" -> "tiff",
    "webp" -> "webp",
  ).withDefaultValue("jpeg")

  case class HTMLImageData(filename: String, base64: String)

  /**
   * 下载图片并转换成 HTML 支持的 base64 格式
   *
   * @param imageURL 图片地址
   * @param timeout  请求超时
   * @param session  请求会话（隐式参数）
   * @return 转换结果
   */
  def downloadPicToHTMLBase64(imageURL: String, timeout: Long = 20000)(implicit session: Session = requests.Session()): HTMLImageData = {
    val data = Base64.getEncoder.encodeToString(session.get(imageURL, readTimeout = 20000).bytes)
    val filename = getLastPartFromURL(imageURL)
    val indexOfPeriod = filename.indexOf('.')
    val prefix = if (indexOfPeriod != -1) {
      val ext = filename.substring(indexOfPeriod + 1, filename.length)
      s"data:image/${BASE64_IMAGE_EXTS(ext.toLowerCase())};base64,"
    } else "data:image/jpeg;base64,"
    HTMLImageData(filename = filename, base64 = prefix + data)
  }

  /**
   * 下载图片并转换成 HTML 支持的 base64 格式
   *
   * @param imageURL 图片地址
   * @param timeout  请求超时
   * @param session  请求会话（隐式参数）
   * @return 转换结果
   */
  def tryDownloadPicToHTMLBase64(imageURL: String, timeout: Long = 20000)(implicit session: Session = requests.Session()): Try[HTMLImageData] =
    Try(downloadPicToHTMLBase64(imageURL, timeout)(session))

  /**
   * 获取 HTML 地址中的最后一段内容
   *
   * @param url 地址
   * @return 最后一段内容
   */
  def getLastPartFromURL(url: String): String = {
    val name = url.substring(url.lastIndexOf('/') + 1)
    val indexOfQuest = name.indexOf('?')
    if (indexOfQuest != -1) name.substring(0, indexOfQuest) else name
  }
}
