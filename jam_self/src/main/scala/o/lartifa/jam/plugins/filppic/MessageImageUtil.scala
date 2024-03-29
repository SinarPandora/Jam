package o.lartifa.jam.plugins.filppic

import at.dhyan.open_imaging.GifDecoder
import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.sender.message.components.ComponentImageBase64
import cc.moecraft.logger.HyLogger
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.GifSequenceWriter
import o.lartifa.jam.pool.JamContext

import java.util.Base64
import scala.util.{Failure, Try}

/**
 * 消息数据
 *
 * Author: sinar
 * 2020/8/29 12:51
 */
object MessageImageUtil {

  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(MessageImageUtil.getClass)

  private case class GIFData(frames: Seq[ImmutableImage], delay: Int, loop: Boolean)

  /**
   * 从消息中获取图片并翻转
   *
   * @param event 消息对象
   * @return 图片数据
   */
  def getAndFlipImageFromMessage(event: EventMessage): Option[ComponentImageBase64] = Try {
    QQImg.parseFromMessage(event).flatMap(flipImage)
      .map(bytes => new ComponentImageBase64(Base64.getEncoder.encodeToString(bytes)))
  }.recoverWith { err =>
    logger.error(s"处理图片失败，消息原文：${event.getMessage}", err)
    Failure(err)
  }.toOption.flatten

  /**
   * 翻转图片
   *
   * @param image 聊天图片对象
   * @return 处理后的图片对象
   */
  private def flipImage(image: QQImg): Option[Array[Byte]] = image.imageType match {
    case QQImg.JPEG | QQImg.PNG =>
      if (useRandomFilter) Some(ImageFilter.randomFilter(ImmutableImage.loader().fromBytes(image.bytes)))
      else flipStaticImage(image)
    case QQImg.GIF => if (useFFMpeg) FFMpegGifFlipper.flip(image) else flipGIFImage(image)
  }

  /**
   * 翻转静态图片
   *
   * @param image 聊天图片对象
   * @return 处理后的图片对象
   */
  private def flipStaticImage(image: QQImg): Option[Array[Byte]] = Some {
    ImmutableImage.loader()
      .fromBytes(image.bytes)
      .flipX()
      .flipY()
      .bytes(image.writer)
  }

  /**
   * 翻转 GIF（有待优化）
   *
   * @param image 聊天图片对象
   * @return 翻转后的 GIF 对象
   */
  private def flipGIFImage(image: QQImg): Option[Array[Byte]] = Some {
    val data = getFlipGifData(image.bytes)
    val writer = new GifSequenceWriter(data.delay, data.loop)
    writer.bytes(data.frames.toArray)
  }

  /**
   * 获取 Gif 全部帧
   *
   * @param imgData GIF 数据
   * @return 帧序列
   */
  private def getFlipGifData(imgData: Array[Byte]): GIFData = {
    val gif = GifDecoder.read(imgData)
    val frames = ((gif.getFrameCount - 1) to 0 by -1).map(gif.getFrame)
      .map(ImmutableImage.fromAwt).map(_.flipX().flipY())
    GIFData(
      frames = frames,
      delay = gif.getDelay(0) * 10,
      // 因为 Stream writer 无法写入指定循环次数，因此直接让他循环一次以上时无限循环
      loop = gif.repetitions != 1
    )
  }


}
