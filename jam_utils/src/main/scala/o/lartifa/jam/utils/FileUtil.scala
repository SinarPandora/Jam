package o.lartifa.jam.utils

import java.io.File

/**
 * 文件工具
 *
 * Author: sinar
 * 2021/1/26 23:38
 */
object FileUtil {

  private val DIR_NOT_EXIST: String = "目录不存在"
  private val FILE_NOT_EXIST: String = "文件不存在"
  private val PATH_NOT_A_DIR: String = "给定路径是一个文件，请指定为文件夹"
  private val PATH_NOT_A_FILE: String = "给定路径是个文件夹，请重新指定为文件"

  /**
   * 转换为文件目录
   *
   * @param path 路径
   * @return 转换结果：
   *         成功：Right(File)
   *         失败：Left(ErrorMessage)
   */
  def castToDir(path: String): Either[String, File] = {
    val dir = new File(path)
    if (!dir.exists()) Left(DIR_NOT_EXIST)
    else if (dir.isFile) Left(PATH_NOT_A_DIR)
    else Right(dir)
  }

  /**
   * 转换为文件
   *
   * @param path 路径
   * @return 转换结果：
   *         成功：Right(File)
   *         失败：Left(ErrorMessage)
   */
  def castToFile(path: String): Either[String, File] = {
    val dir = new File(path)
    if (!dir.exists()) Left(DIR_NOT_EXIST)
    else if (dir.isDirectory) Left(PATH_NOT_A_FILE)
    else Right(dir)
  }
}
