package o.lartifa.jam.common.util

/**
 * 常用错误信息
 *
 * Author: sinar
 * 2021/1/26 23:03
 */
object ErrorMsg {
  val USER_UN_AUTHED: String = "用户未登录"
  val PLEASE_RETRY: String = "出现了一些错误，请稍后重试"

  object IO {
    val DIR_NOT_EXIST: String = "目录不存在"
    val FILE_NOT_EXIST: String = "文件不存在"
    val PATH_NOT_A_DIR: String = "给定路径是一个文件，请指定为文件夹"
    val PATH_NOT_A_FILE: String = "给定路径是个文件夹，请重新指定为文件"
  }
}
