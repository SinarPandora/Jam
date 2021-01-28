package o.lartifa.jam.service

import better.files._
import o.lartifa.jam.common.util.ErrorMsg
import o.lartifa.jam.service.VCService.{VCSFileStruct, VCSResult}
import o.lartifa.jam.utils.FileUtil
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.revwalk.RevCommit
import org.owasp.esapi.{ESAPI, Logger}
import org.springframework.stereotype.Service

import java.io.File
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions
import scala.util.Try

/**
 * 基于 Git 的版本管理服务
 *
 * Author: sinar
 * 2021/1/23 10:55
 */
@Service
class GitVCService(userService: UserService) extends VCService[RevCommit] {

  /**
   * 将目录视为仓库
   *
   * @param path 文件目录地址
   * @return 转换结果
   */
  private implicit class pathASRepo(path: String) {
    def toRepo: Either[String, Git] = FileUtil.castToDir(path).flatMap(dir => {
      Try(Git.open(dir)).toEither.left.map {
        case _: RepositoryNotFoundException => "当前目录没有被初始化"
        case e => throw e
      }
    })
  }

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
  override def init(path: String): VCSResult =
    FileUtil.castToDir(path).map { dir =>
      val repo: Git = Git.init().setDirectory(dir).call()
      createIgnoreFile(dir)
      repo.add().addFilepattern(".").call()
      repo
    }.flatMap(commit(_, "首次保存"))

  /**
   * 将当前版本作为一次提交，并打 tag
   *
   * @param tag  tag 名称
   * @param path 指定目录
   * @return 操作结果
   */
  override def tag(tag: String, path: String): VCSResult =
    path.toRepo.map(commit(_, tag))

  /**
   * 列出当前全部 tag 记录
   *
   * @param path 指定目录
   * @return 全部 tag 记录
   */
  def listTags(path: String): Either[String, Iterable[RevCommit]] =
    path.toRepo.map(_.log().call().asScala)

  /**
   * 退回到指定版本
   *
   * @param rollbackTo 目标 tag
   * @param path       指定目录
   * @return 操作结果
   */
  override def rollback(rollbackTo: String, path: String): VCSResult =
    path.toRepo.map(_.reset().setRef(rollbackTo).call())

  /**
   * 获取当前版本 tag
   *
   * @param path 指定目录
   * @return 当前 tag
   */
  override def currentTag(path: String): Option[RevCommit] =
    path.toRepo.toOption.flatMap { repo =>
      val logIter = repo.log().call().iterator()
      if (logIter.hasNext) Some(logIter.next())
      else None
    }

  /**
   * 备份路径下全部文件
   *
   * @param path   指定目录
   * @param target 备份路径
   * @return 操作结果
   */
  override def backup(path: String, target: String): VCSResult = {
    FileUtil.castToDir(path)
      .flatMap(it => FileUtil.castToDir(target).map(it -> _))
      .map {
        case (from, to) => from.toScala.zipTo(to.toScala)
      }
  }

  /**
   * 获取当前被更改的全部文件
   *
   * @param path 指定目录
   * @return 更改的文件
   */
  override def currentChanges(path: String): Either[String, List[VCSFileStruct]] = ???

  /**
   * 创建 ignore 文件
   *
   * @param rootDir 根目录
   * @return 操作结果
   */
  private def createIgnoreFile(rootDir: File): Unit = {
    val ignoreFile = (rootDir.toScala / ".gitignore").createIfNotExists()
    ignoreFile.appendLines("# 以下类型的文件会被忽略", ".DS_Store", ".AppleDouble", ".LSOverride", "Icon", "._*",
      ".DocumentRevisions-V100", ".fseventsd", ".Spotlight-V100", ".TemporaryItems", ".Trashes",
      ".VolumeIcon.icns", ".com.apple.timemachine.donotpresent", ".AppleDB", ".AppleDesktop",
      "Network Trash Folder", "Temporary Items", ".apdisk", "*.bak", "*.gho", "*.ori", "*.orig",
      "*.tmp", "Thumbs.db", "Thumbs.db:encryptable", "ehthumbs.db", "ehthumbs_vista.db", "*.stackdump",
      "[Dd]esktop.ini", "$RECYCLE.BIN/", "*.cab", "*.msi", "*.msix", "*.msm", "*.msp", "*.lnk")
  }

  /**
   * 重置当前文件夹的版本控制系统
   *
   * @param path 指定目录
   * @return 操作结果
   */
  override def reset(path: String): VCSResult =
    FileUtil.castToDir(path).flatMap { dir =>
      val gitDir = dir.toScala / ".git"
      if (gitDir.exists) Right(gitDir.delete())
      else Left("该目录并未被版本控制系统接管")
    }

  /**
   * Git 提交
   *
   * @param repo    仓库
   * @param message 提交信息
   * @return 操作结果
   */
  private def commit(repo: Git, message: String): Either[String, Unit] = {
    userService.getSessionUsername match {
      case None => Left(ErrorMsg.USER_UN_AUTHED)
      case Some(username) =>
        repo.add().addFilepattern(".").call()
        repo.commit().setAuthor(username, s"$username@jam.mgt.user").setMessage(message).call()
        Right()
    }
  }
}

object GitVCService {
  private val logger: Logger = ESAPI.getLogger(classOf[GitVCService])
}
