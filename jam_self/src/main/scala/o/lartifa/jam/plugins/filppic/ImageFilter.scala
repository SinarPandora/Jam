package o.lartifa.jam.plugins.filppic

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.angles.Degrees
import com.sksamuel.scrimage.color.X11Colorlist
import com.sksamuel.scrimage.filter.MotionBlurFilter
import com.sksamuel.scrimage.nio.GifSequenceWriter

import java.awt
import scala.util.Random

/**
 * 图片滤镜
 *
 * Author: sinar
 * 2021/12/6 22:26
 */
object ImageFilter {
  private val gifWriter: GifSequenceWriter = new GifSequenceWriter()
    .withFrameDelay(100)
    .withInfiniteLoop(true)
  private val BLACK: awt.Color = X11Colorlist.Black.toAWT

  /**
   * 随机过滤
   *
   * @param image 原图
   * @return gif 数据
   */
  def randomFilter(image: ImmutableImage): Array[Byte] = {
    val filter = Random.nextInt(7) match {
      case 0 => flip _
      case 1 => loop _
      case 2 => motionBlur _
      case 3 => motionBlurXY _
      case 4 => faceToFace _
      case 5 => headToFoot _
      case 6 => twitch _
    }
    filter(image)
  }

  /**
   * 滤镜：翻转
   *
   * @param image 原图
   * @return gif 数据
   */
  def flip(image: ImmutableImage): Array[Byte] = {
    gifWriter.bytes(Array(
      image.flipX().flipY()
    ))
  }

  /**
   * 滤镜：图片循环旋转
   *
   * @param image 原图
   * @return gif 数据
   */
  def loop(image: ImmutableImage): Array[Byte] = {
    val (side, rate) = if (image.height > image.width) {
      (image.height, image.width / image.height)
    } else {
      (image.width, image.height / image.width)
    }
    val scaleImage = if (rate >= 0.6) {
      image.cover(side, side)
    } else {
      image.copy().fit(side, side, BLACK)
    }
    gifWriter.bytes(Array(
      scaleImage.flipX().flipY(),
      scaleImage.rotate(new Degrees(-90)),
      scaleImage,
      scaleImage.rotate(new Degrees(90))
    ))
  }

  /**
   * 滤镜：单方向动感模糊
   *
   * @param image 原图
   * @return gif 数据
   */
  def motionBlur(image: ImmutableImage): Array[Byte] = {
    gifWriter.bytes(Array(
      image.filter(new MotionBlurFilter(0, 2)),
      image.filter(new MotionBlurFilter(0, 4)),
      image.filter(new MotionBlurFilter(0, 6)),
      image.filter(new MotionBlurFilter(0, 8)),
      image.filter(new MotionBlurFilter(0, 10))
    ))
  }

  /**
   * 滤镜：多方向动感模糊
   *
   * @param image 原图
   * @return gif 数据
   */
  def motionBlurXY(image: ImmutableImage): Array[Byte] = {
    gifWriter.bytes(Array(
      image.filter(new MotionBlurFilter(0, 8)),
      image.filter(new MotionBlurFilter(45, 8)),
      image.filter(new MotionBlurFilter(90, 8)),
      image.filter(new MotionBlurFilter(135, 8)),
      image.filter(new MotionBlurFilter(180, 8)),
      image.filter(new MotionBlurFilter(225, 8)),
      image.filter(new MotionBlurFilter(270, 8)),
      image.filter(new MotionBlurFilter(315, 8))
    ))
  }

  /**
   * 滤镜：脸对脸
   *
   * @param image 原图
   * @return gif 数据
   */
  def faceToFace(image: ImmutableImage): Array[Byte] = {
    gifWriter.bytes(Array(
      image,
      image.flipX(),
    ))
  }

  /**
   * 滤镜：头对脚
   *
   * @param image 原图
   * @return gif 数据
   */
  def headToFoot(image: ImmutableImage): Array[Byte] = {
    gifWriter.bytes(Array(
      image,
      image.flipY(),
    ))
  }

  /**
   * 抽动（放大，缩小，放大）
   *
   * @param image 原图
   * @return gif 数据
   */
  def twitch(image: ImmutableImage): Array[Byte] = {
    gifWriter.bytes(Array(
      image.zoom(2),
      image,
    ))
  }
}
