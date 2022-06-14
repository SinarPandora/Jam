package o.lartifa.jam.common.util

/**
 * 系统工具
 *
 * Author: sinar
 * 2022/6/14 23:27
 */
object OSUtil {
  /**
   * 获取操作系统名
   *
   * @return 操作系统名称
   */
  def os: String = System.getProperty("os.name")

  /**
   * 当前操作系统是否为 Windows
   *
   * @return 判断结果
   */
  def isWindows: Boolean = {
    System.getProperty("os.name").toLowerCase.contains("win")
  }
}
