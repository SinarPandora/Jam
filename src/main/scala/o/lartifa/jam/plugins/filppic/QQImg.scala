package o.lartifa.jam.plugins.filppic

import java.util.NoSuchElementException

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.logger.HyLogger
import com.sksamuel.scrimage.nio.{ImageWriter, JpegWriter, PngWriter}
import o.lartifa.jam.plugins.filppic.QQImg.{ImageType, logger}
import o.lartifa.jam.pool.JamContext

import scala.util.matching.Regex
import scala.util.{Failure, Try}

/**
 * QQ 聊天图片
 *
 * Author: sinar
 * 2020/8/29 17:50
 */
case class QQImg(url: String, imageType: ImageType) {
  // 图片比特数据
  lazy val bytes: Option[Array[Byte]] =
    Try(requests.get(url).bytes).recoverWith(err => {
      logger.error("下载聊天图片失败", err)
      Failure(err)
    }).toOption

  lazy val writer: ImageWriter = {
    this.imageType match {
      case QQImg.JPEG => JpegWriter.NoCompression
      case QQImg.PNG => PngWriter.NoCompression
      case QQImg.GIF =>
        throw new NoSuchElementException("默认的 Writer 只会使用第一张静态帧，请用 com.sksamuel.scrimage.nio.StreamingGifWriter 代替")
    }
  }
}


object QQImg {
  private lazy val logger: HyLogger = JamContext.logger.get()

  sealed abstract class ImageType(val exts: List[String])
  case object JPEG extends ImageType(List("jpg", "jpeg"))
  case object PNG extends ImageType(List("png"))
  case object GIF extends ImageType(List("gif"))

  // Example [CQ:image,file=40A.jpg,url=https://gchat.qpic.cn/gchatpic_new/39/70-A/0?term=2]整个人都是麻的
  private val CQ_IMAGE_PATTERN: Regex = """\[CQ:image,file=(.+?),url=(.+?)]""".r("filename", "url")

  /**
   * 从消息对象解析聊天图片
   *
   * @param event 消息对象
   * @return 可能存在的聊天图片
   */
  def parseFromMessage(event: EventMessage): Option[QQImg] = {
    CQ_IMAGE_PATTERN.findFirstMatchIn(event.message.replaceAll("""\s""", ""))
      .map { it =>
        val imageType = it.group("filename").toLowerCase match {
          case str if JPEG.exts.contains(str) => JPEG
          case str if PNG.exts.contains(str) => PNG
          case str if GIF.exts.contains(str) => GIF
          case other =>
            logger.warning(s"聊天中出现了暂不支持的图片类型：$other")
            return None
        }
        val url = it.group("url")
        QQImg(url, imageType)
      }
  }
}
