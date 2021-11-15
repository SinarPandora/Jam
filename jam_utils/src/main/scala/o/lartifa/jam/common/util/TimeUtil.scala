package o.lartifa.jam.common.util

import java.sql.Timestamp
import java.time.Instant

/**
 * 时间相关的工具类
 *
 * Author: sinar
 * 2020/1/14 23:27
 */
object TimeUtil {

  /**
   * 获取当前时间戳
   * 同步同名 SQL 函数
   *
   * @return 当前时间戳
   */
  def currentTimeStamp: Timestamp = Timestamp.from(Instant.now())

  /**
   * 将纪元秒转换为时间戳
   *
   * @param milliseconds 纪元秒
   * @return 时间戳
   */
  def epochMilliToTimeStamp(milliseconds: Long): Timestamp = new Timestamp(milliseconds)
}
