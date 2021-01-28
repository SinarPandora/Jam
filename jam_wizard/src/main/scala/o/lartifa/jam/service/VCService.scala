package o.lartifa.jam.service

import o.lartifa.jam.service.VCService.{VCSFileStruct, VCSResult}

/**
 * 文件版本管理服务
 *
 * Author: sinar
 * 2021/1/23 10:55
 */
trait VCService[Tag] {

  /**
   * 为目录初始化版本控制系统
   *
   * @param path 指定目录
   * @return 初始化结果
   */
  def init(path: String): VCSResult

  /**
   * 将当前版本作为一次提交，并打 tag
   *
   * @param tag  tag 名称
   * @param path 指定目录
   * @return 操作结果
   */
  def tag(tag: String, path: String): VCSResult

  /**
   * 列出当前全部 tag 记录
   *
   * @param path 指定目录
   * @return 全部 tag 记录
   */
  def listTags(path: String): Either[String, Iterable[Tag]]

  /**
   * 退回到指定版本
   *
   * @param rollbackTo 目标 tag
   * @param path       指定目录
   * @return 操作结果
   */
  def rollback(rollbackTo: String, path: String): VCSResult

  /**
   * 获取当前版本 tag
   *
   * @param path 指定目录
   * @return 当前 tag
   */
  def currentTag(path: String): Option[Tag]

  /**
   * 备份路径下全部文件
   *
   * @param path   指定目录
   * @param target 备份路径
   * @return 操作结果
   */
  def backup(path: String, target: String): VCSResult

  /**
   * 获取当前被更改的全部文件
   *
   * @param path 指定目录
   * @return 更改的文件
   */
  def currentChanges(path: String): Either[String, List[VCSFileStruct]]

  /**
   * 重置当前文件夹的版本控制系统
   *
   * @param path 指定目录
   * @return 操作结果
   */
  def reset(path: String): VCSResult
}

object VCService {
  type VCSResult = Either[String, Unit]

  object VCSFileStatus {
    val Added: String = "Added"
    val Changed: String = "Changed"
    val Removed: String = "Removed"
    val Missing: String = "Missing"
    val Modified: String = "Modified"
    val Conflicting: String = "Conflicting"
  }

  case class VCSFileStruct
  (
    name: String,
    path: String,
    isDir: Boolean,
    status: String,
    subDirs: List[VCSFileStruct] = Nil
  )
}
