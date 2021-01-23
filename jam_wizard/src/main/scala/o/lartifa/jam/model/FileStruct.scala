package o.lartifa.jam.model

/**
 * 文件结构
 *
 * Author: sinar
 * 2021/1/23 10:26
 */
case class FileStruct
(
  name: String,
  path: String,
  isDir: Boolean,
  subDirs: List[FileStruct] = Nil
)
