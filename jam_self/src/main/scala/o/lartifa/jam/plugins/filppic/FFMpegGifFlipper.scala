package o.lartifa.jam.plugins.filppic

import better.files._
import cc.moecraft.logger.HyLogger
import com.github.kokorin.jaffree.ffmpeg.{FFmpeg, UrlInput, UrlOutput}
import o.lartifa.jam.common.config.SystemConfig
import o.lartifa.jam.pool.JamContext

import java.nio.file.Path
import scala.util.{Failure, Try}

/**
 * FFMpeg Gif 图片翻转器
 * Author: sinar
 * 2021/5/9 20:27
 */
object FFMpegGifFlipper {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(FFMpegGifFlipper.getClass)
  private val tmp: File = (File(SystemConfig.tempDir) / "flip_pic_cache").createDirectoryIfNotExists()

  /**
   * 获取 FFMpeg 实例
   *
   * @return 实例
   */
  private def ffmpeg: FFmpeg = {
    if (ffmpegPath.isBlank) FFmpeg.atPath()
    else FFmpeg.atPath(Path.of(ffmpegPath))
  }

  /**
   * 翻转图片
   *
   * @param image 聊天图片
   * @return 翻转后的图片数据
   */
  def flip(image: QQImg): Option[Array[Byte]] = Try {
    useCacheOrCreateTmpFile(image) match {
      case Left(data) => data
      case Right(source) => flipImgViaFFMpeg(source)
    }
  }.recoverWith { err =>
    logger.error(s"FFMpeg 处理图片失败", err)
    Failure(err)
  }.toOption

  /**
   * 如果存在处理过的图片，直接读取；否则翻转图片
   *
   * @param image 聊天图片
   * @return 存在缓存：缓存
   *         不存在缓存：输出输出路径
   */
  private def useCacheOrCreateTmpFile(image: QQImg): Either[Array[Byte], (UrlInput, File)] = {
    val cache = tmp / (image.filename + "_rev.gif")
    if (cache.exists) Left(cache.byteArray)
    else {
      val source = (tmp / (image.filename + ".gif")).createFileIfNotExists()
      source.writeByteArray(image.bytes)
      Right((UrlInput.fromUrl(source.pathAsString), cache))
    }
  }

  /**
   * 通过 FFMpeg 翻转图片
   *
   * @param source 输入输出路径
   * @return 翻转后的图片数据
   */
  private def flipImgViaFFMpeg(source: (UrlInput, File)): Array[Byte] = {
    val (in: UrlInput, out: File) = source
    ffmpeg.addInput(in)
      .addOutput(UrlOutput.toUrl(out.pathAsString))
      .addArguments("-vf", "reverse,hflip,vflip")
      .execute()
    out.byteArray
  }


}
