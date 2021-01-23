package o.lartifa.jam.service

import org.springframework.stereotype.Service

import scala.util.Try

/**
 * 文件版本管理服务
 *
 * Author: sinar
 * 2021/1/23 10:55
 */
trait VCService {

  /**
   * 为目录初始化版本控制系统
   *
   * @param path 指定目录
   * @return 初始化结果
   */
  def init(path: String): Try[Unit]

  /**
   * 将当前版本作为一次提交，并打 tag
   *
   * @param tag  tag 名称
   * @param path 指定目录
   * @return 操作结果
   */
  def tag(tag: String, path: String): Try[Unit]

  /**
   * 列出当前全部 tag 记录
   *
   * @param path 指定目录
   * @return 操作结果
   */
  def listTags(path: String): Try[Unit]

  /**
   * 退回到指定版本
   *
   * @param rollbackTo 目标 tag
   * @param path       指定目录
   * @return 操作结果
   */
  def rollback(rollbackTo: String, path: String): Try[Unit]

  /**
   * 获取当前版本 tag
   *
   * @param path 指定目录
   * @return 当前 tag
   */
  def currentTag(path: String): Option[String]

  /**
   * 备份路径下全部文件
   *
   * @param path   指定目录
   * @param target 备份路径
   * @return 操作结果
   */
  def backup(path: String, target: String): Try[Unit]
}

@Service
class GitVCService extends VCService {
  /**
   * 为目录初始化版本控制系统
   *
   * @param path 指定目录
   * @return 初始化结果
   */
  override def init(path: String): Try[Unit] = ???

  /**
   * 将当前版本作为一次提交，并打 tag
   *
   * @param tag  tag 名称
   * @param path 指定目录
   * @return 操作结果
   */
  override def tag(tag: String, path: String): Try[Unit] = ???

  /**
   * 列出当前全部 tag 记录
   *
   * @param path 指定目录
   * @return 操作结果
   */
  override def listTags(path: String): Try[Unit] = ???

  /**
   * 退回到指定版本
   *
   * @param rollbackTo 目标 tag
   * @param path       指定目录
   * @return 操作结果
   */
  override def rollback(rollbackTo: String, path: String): Try[Unit] = ???

  /**
   * 获取当前版本 tag
   *
   * @param path 指定目录
   * @return 当前 tag
   */
  override def currentTag(path: String): Option[String] = ???

  /**
   * 备份路径下全部文件
   *
   * @param path   指定目录
   * @param target 备份路径
   * @return 操作结果
   */
  override def backup(path: String, target: String): Try[Unit] = ???
}
