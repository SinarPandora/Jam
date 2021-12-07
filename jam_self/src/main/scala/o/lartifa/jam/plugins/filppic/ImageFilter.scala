package o.lartifa.jam.plugins.filppic

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.angles.Degrees
import com.sksamuel.scrimage.filter.MotionBlurFilter
import com.sksamuel.scrimage.nio.GifSequenceWriter

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

  /**
   * 随机过滤
   *
   * @param image 原图
   * @return gif 数据
   */
  def randomFilter(image: ImmutableImage): Array[Byte] = {
    val filter = Random.nextInt(4) match {
      case 0 => flip _
      case 1 => loop _
      case 2 => motionBlur _
      case 3 => motionBlurXY _
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
    val side = if (image.height > image.width) image.width else image.height
    val scaleImage = image.copy().cover(side, side)
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
      image.filter(new MotionBlurFilter(0, 10)),
      image.filter(new MotionBlurFilter(0, 20)),
      image.filter(new MotionBlurFilter(0, 30)),
      image.filter(new MotionBlurFilter(0, 40)),
      image.filter(new MotionBlurFilter(0, 50))
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
      image.filter(new MotionBlurFilter(0, 10)),
      image.filter(new MotionBlurFilter(45, 10)),
      image.filter(new MotionBlurFilter(90, 10)),
      image.filter(new MotionBlurFilter(135, 10)),
      image.filter(new MotionBlurFilter(180, 10)),
      image.filter(new MotionBlurFilter(225, 10)),
      image.filter(new MotionBlurFilter(270, 10)),
      image.filter(new MotionBlurFilter(315, 10))
    ))
  }
}
