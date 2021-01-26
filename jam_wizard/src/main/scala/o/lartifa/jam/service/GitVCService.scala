package o.lartifa.jam.service

import o.lartifa.jam.model.FileStruct
import org.eclipse.jgit.api.Git
import org.springframework.stereotype.Service

import java.io.File
import scala.util.Try

/**
 * 基于 Git 的版本管理服务
 *
 * Author: sinar
 * 2021/1/23 10:55
 */
@Service
class GitVCService extends VCService {
  // TODO 完备的测试
  /**
   * 为目录初始化版本控制系统
   * > git init
   * > create .ignore_file
   * > git all --all
   * > git commit -m xxx
   *
   * @param path 指定目录
   * @return 初始化结果
   */
  override def init(path: String): Try[Unit] = {
    val repo = Git.init().setDirectory(new File(path)).call()
    repo.add().addFilepattern(".").call()
    // repo.commit().setMessage().setAuthor()
    ???
  }

  /**
   * 将当前版本作为一次提交，并打 tag
   * > git add --all
   * > git commit -m xxx
   *
   * @param tag  tag 名称
   * @param path 指定目录
   * @return 操作结果
   */
  override def tag(tag: String, path: String): Try[Unit] = ???

  /**
   * 列出当前全部 tag 记录
   * > git log
   *
   * @param path 指定目录
   * @return 操作结果
   */
  override def listTags(path: String): Try[Unit] = ???

  /**
   * 退回到指定版本
   * > git checkout
   *
   * @param rollbackTo 目标 tag
   * @param path       指定目录
   * @return 操作结果
   */
  override def rollback(rollbackTo: String, path: String): Try[Unit] = ???

  /**
   * 获取当前版本 tag
   * >
   *
   * @param path 指定目录
   * @return 当前 tag
   */
  override def currentTag(path: String): Option[String] = ???

  /**
   * 备份路径下全部文件
   * > use zip
   *
   * @param path   指定目录
   * @param target 备份路径
   * @return 操作结果
   */
  override def backup(path: String, target: String): Try[Unit] = ???

  /**
   * 获取当前被更改的全部文件
   *
   * @param path 指定目录
   * @return 更改的文件
   */
  override def currentChanges(path: String): List[FileStruct] = ???

  /**
   * 创建 ignore 文件
   *
   * @param rootPath 根目录
   * @return 操作结果
   */
  private def createIgnoreFile(rootPath: String): Try[Unit] = {
    ???
  }

  /**
   * 重置当前文件夹的版本控制系统
   *
   * @param path 指定目录
   * @return 操作结果
   */
  override def reset(path: String): Try[Unit] = ???
}
