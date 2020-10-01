package o.lartifa.jam.plugins

import java.util.concurrent.atomic.AtomicReference

import cc.moecraft.logger.{HyLogger, LogLevel}
import cn.hutool.core.util.StrUtil
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.database.temporary.Memory.database.db
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.plugins.api.JamPluginInstaller
import o.lartifa.jam.pool.JamContext
import org.reflections.Reflections

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Try}

/**
 * Jam 插件加载器
 *
 * Author: sinar
 * 2020/10/1 03:31
 */
object JamPluginLoader {

  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(JamPluginLoader.getClass)

  import o.lartifa.jam.database.temporary.Memory.database.profile.api._

  case class LoadedInitializers
  (
    // TODO
  )

  /**
   * 装载好的加载器实例们
   * 系统读取加载器时不再需要重新创建，而是直接从这里获取
   */
  val loadedInitializers: AtomicReference[LoadedInitializers] = new AtomicReference()

  /**
   * 加载果酱的插件系统
   *
   * @param exec 异步执行上下文
   */
  def initJamPluginSystems()(implicit exec: ExecutionContext): Future[Unit] = async {
    // 反射搜索 JamPluginInstaller
    val installers = scanPlugins()
    // 对比存在的插件和数据库表
    val installedPlugins = await(db.run(Plugins.result)).map(it => it.`package` -> it).toMap
    val installation = await {
      Future.sequence {
        installers
          .filterNot(it => installedPlugins.keySet.contains(it._1)) // 若表中不存在，执行 install
          .map { case (packageName, installer) => tryInstallPlugin(packageName, installer) }
      }
    }
    val successInstallation = installation.filter(_._2.isSuccess).map(_._1)
    val needInsert = (installers -- successInstallation)
      .map {
        case (packageName, it) => (it.pluginName, it.keywords.mkString(","), it.author, packageName, JamConfig.autoEnablePlugins)
      }.toList

    val insertSuccess = needInsert.sizeIs == await {
      db.run {
        Plugins.map(row => (row.name, row.keywords, row.author, row.`package`, row.isEnabled)) ++= needInsert
      }
    }.getOrElse(0)

    if (!insertSuccess) {
      logger.warning(s"插件安装数量与预期不符，这很可能是一个 bug，若${JamConfig.name}无法正常运作，请联系作者")
      MasterUtil.notifyMaster(s"插件安装数量与预期不符，这很可能是一个 bug，若${JamConfig.name}无法正常运作，请联系作者")
    }

    // 若表中存在而插件未找到，提示警告

    // 将挂载点注入到各个组件


  }

  /**
   * 扫描系统中全部的插件
   *
   * @return 插件类路径 -> 插件实例映射
   */
  private def scanPlugins(): Map[String, JamPluginInstaller] = {
    new Reflections(StrUtil.EMPTY)
      .getSubTypesOf(classOf[JamPluginInstaller]).asScala.toList
      .map(it => it.getName -> it.getDeclaredConstructor().newInstance())
      .toMap
  }

  /**
   * 尝试安装插件
   *
   * @param packageName 包名
   * @param installer   安装器
   * @param exec        异步上下文
   * @return 安装结果
   */
  private def tryInstallPlugin(packageName: String, installer: JamPluginInstaller)(implicit exec: ExecutionContext): Future[(String, Try[Unit])] = {
    installer.install().recoverWith(err => {
      MasterUtil.notifyAndLog(s"[${installer.pluginName}]安装插件出错，正在自动尝试卸载...",
        LogLevel.ERROR, Some(err))
      MasterUtil.notifyMaster(s"插件包名为：$packageName")
      installer.uninstall().recover(err => {
        MasterUtil.notifyAndLog(s"[${installer.pluginName}]安装卸载失败，请删除该插件并可以尝试联系插件作者",
          LogLevel.ERROR, Some(err))
        MasterUtil.notifyMaster(s"插件包名为：$packageName")
        Failure(err)
      })
    }).map(it => packageName -> it)
  }

  /**
   * 启用插件
   *
   * @param id 插件 ID
   * @return 启用是否成功
   */
  def enablePlugin(id: Int): Future[Boolean] = ???

  /**
   * 禁用插件
   *
   * @param id 插件 ID
   * @return 禁用是否成功
   */
  def disablePlugin(id: Int): Future[Boolean] = ???

  /**
   * 卸载插件
   *
   * @param id 插件 ID
   * @return 卸载是否成功
   */
  def uninstallPlugin(id: Int): Future[Boolean] = ???
}
