package o.lartifa.jam.engine

import better.files.File
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.BotConfig.RemoteEditing
import o.lartifa.jam.common.config.SystemConfig
import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.pool.JamContext
import o.lartifa.jam.utils.GitUtil
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.transport.{URIish, UsernamePasswordCredentialsProvider}

import java.io.File as JFile
import java.nio.file.Paths
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

/**
 * 远程 SXDL 客户端
 * Author: sinar
 * 2021/2/27 20:51
 */
object RemoteSXDLClient {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(RemoteEditing.getClass)
  private val credential: UsernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(RemoteEditing.username, RemoteEditing.secret)

  /**
   * 更新远程 SXDL 脚本
   *
   * @param scriptPath SXDL 脚本路径
   * @param exec       执行上下文
   * @return 操作结果
   */
  def fetchRemoteScripts(scriptPath: File)(implicit exec: ExecutionContext): Future[Unit] = Future {
    val scriptPathJ = scriptPath.toJava
    val repo = new URIish(RemoteEditing.repo)
    val isGitDir = Paths.get(SystemConfig.sxdlPath, ".git").toFile.exists()
    if (!isGitDir) {
      initAndPushToRemote(scriptPathJ, repo).recoverWith { err =>
        MasterUtil.notifyMaster(
          "%s，无法为本地脚本目录初始化版本控制，您可以尝试关闭远程编辑或删除 SXDL 脚本目录下的 .git 文件夹（可能需要显示隐藏文件）"
        )
        logger.error(err)
        throw ParseFailException("无法为本地脚本目录初始化版本控制")
      }
    }
    getLatestScripts(Git.open(scriptPathJ)).recoverWith { err =>
      MasterUtil.notifyMaster(
        "%s，现在无法从远程获取最新的脚本，为了保证 bot 正常运行，该错误会被忽略。请稍后检查 bot 所在服务器的网络状态。"
      )
      logger.error(err)
      Success(())
    }
  }

  /**
   * 丢弃本地更改并获取远端最新脚本文件
   *
   * @param repo 远程仓库地址
   * @return 操作结果
   */
  def getLatestScripts(repo: Git): Try[Unit] = Try {
    logger.log("正在将本地文件更改同步到与远程一致")
    repo.reset().setMode(ResetType.HARD).call()
    logger.log("正在拉取远程文件")
    repo.pull().setCredentialsProvider(credential).call()
    logger.log("同步完成！")
  }

  /**
   * 为本地脚本文件夹初始化版本控制，并推送到远端仓库
   *
   * @param path   脚本目录
   * @param remote 远端地址
   * @return 操作结果
   */
  def initAndPushToRemote(path: JFile, remote: URIish): Try[Unit] = Try {
    logger.log("初始化 git 目录")
    val repo: Git = Git.init().setDirectory(path).call()
    GitUtil.createIgnoreFile(path)
    repo.add().addFilepattern(".").call()
    logger.log("已添加全部文件，正在尝试提交到远程仓库")
    repo.commit().setAuthor(RemoteEditing.username, RemoteEditing.email).setMessage("初始化").call()
    repo.branchCreate().setName(RemoteEditing.branch).call()
    repo.remoteAdd().setName("origin").setUri(remote).call()
    repo.push().setCredentialsProvider(credential).call()
    logger.log("本地脚本目录与远程仓库同步完成")
  }
}
