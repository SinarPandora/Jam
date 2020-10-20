package o.lartifa.jam.plugins

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.logger.{HyLogger, LogLevel}
import cn.hutool.core.util.StrUtil
import o.lartifa.jam.common.config.{JamConfig, JamPluginConfig}
import o.lartifa.jam.common.util.{MasterUtil, TimeUtil}
import o.lartifa.jam.cool.qq.command.base.MasterEverywhereCommand
import o.lartifa.jam.cool.qq.listener.prehandle.PreHandleTask
import o.lartifa.jam.database.temporary.Memory.database.db
import o.lartifa.jam.database.temporary.schema.Tables
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.engine.JamLoader
import o.lartifa.jam.engine.parser.SSDLCommandParser
import o.lartifa.jam.engine.parser.SSDLCommandParser._
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.model.tasks.JamCronTask.TaskDefinition
import o.lartifa.jam.model.tasks.LifeCycleTask
import o.lartifa.jam.plugins.api.JamPluginInstaller
import o.lartifa.jam.pool.JamContext
import org.reflections.Reflections

import scala.async.Async.{async, await}
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

/**
 * Jam 插件加载器
 *
 * Author: sinar
 * 2020/10/1 03:31
 */
object JamPluginLoader {

  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(JamPluginLoader.getClass)

  import o.lartifa.jam.database.temporary.Memory.database.profile.api._

  case class LoadedComponents
  (
    bootTasks: List[LifeCycleTask] = Nil,
    shutdownTasks: List[LifeCycleTask] = Nil,
    preHandleTasks: List[PreHandleTask] = Nil,
    containsModeCommandParsers: List[SSDLCommandParser[_, Command[_]]] = Nil,
    regexModeCommandParsers: List[SSDLCommandParser[_, Command[_]]] = Nil,
    highOrderModeCommandParsers: List[SSDLCommandParser[_, Command[_]]] = Nil,
    cronTaskDefinitions: List[TaskDefinition] = Nil,
    masterCommands: List[MasterEverywhereCommand] = Nil,
    afterSleepTasks: List[LifeCycleTask] = Nil
  )

  /**
   * 装载好的加载器实例们
   * 系统读取加载器时不再需要重新创建，而是直接从这里获取
   */
  private var _loadedComponents: LoadedComponents = LoadedComponents()

  def loadedComponents: LoadedComponents = this._loadedComponents

  /**
   * 插件类路径 -> 插件实例映射
   */
  lazy val installers: Map[String, JamPluginInstaller] = scanPlugins()

  /**
   * 加载果酱的插件系统
   *
   * @param exec 异步执行上下文
   */
  def initJamPluginSystems()(implicit exec: ExecutionContext): Future[Unit] = async {
    // 对比存在的插件和数据库表
    val installedPlugins: Map[String, Tables.PluginsRow] = await(db.run(Plugins.result)).map(it => it.`package` -> it).toMap
    if (installers.nonEmpty || installedPlugins.nonEmpty) {
      // 若表中存在而插件未找到，提示警告
      val missingPlugins = (installedPlugins -- installers.keySet).map {
        case (packageName, record) => s"名称：${record.name}，作者：${record.author}，包名：$packageName"
      }.mkString("\n")
      if (missingPlugins.nonEmpty) {
        logger.warning(s"以下插件丢失：$missingPlugins")
        MasterUtil.notifyMaster(s"[警告⚠️] 以下插件丢失（他们已经无法找到并没有被正确卸载）：")
        MasterUtil.notifyMaster(missingPlugins)
      }

      // 自动安装插件
      val installResult = await(autoInstall(installedPlugins.keySet))
      // 自动升级插件
      await(autoUpgrade(installedPlugins))

      // 将挂载点注入到各个组件
      val needLoad = installedPlugins.values.groupBy(_.isEnabled).getOrElse(true, Nil).map(_.`package`) ++ {
        if (JamPluginConfig.autoEnablePlugins) installResult.getOrElse(true, Nil)
        else Nil
      }
      this._loadedComponents = mountPlugins((installers -- needLoad).values)
    }
  }

  /**
   * 自动安装表中不存在的插件
   *
   * @param installedPluginNames 已安装的插件名称
   * @param exec                 异步执行上下文
   * @return 安装结果
   */
  private def autoInstall(installedPluginNames: Set[String])(implicit exec: ExecutionContext): Future[Map[Boolean, Iterable[String]]] = async {
    val installation = await {
      Future.sequence {
        installers
          .filterNot(it => installedPluginNames.contains(it._1)) // 若表中不存在，执行 install
          .map { case (packageName, installer) => tryInstallPlugin(packageName, installer) }
      }
    }
    val installResult = installation.groupMap(_._2.isSuccess)(_._1)
    val needInsert = (installers -- installResult.getOrElse(true, Nil))
      .map { case (packageName, it) =>
        (it.pluginName, it.keywords.mkString(","), it.author, packageName, JamPluginConfig.autoEnablePlugins)
      }.toList

    val insertSuccess = needInsert.sizeIs == await {
      db.run {
        Plugins.map(row => (row.name, row.keywords, row.author, row.`package`, row.isEnabled)) ++= needInsert
      }
    }.getOrElse(0)

    if (!insertSuccess) {
      MasterUtil.notifyAndLog(s"插件安装数量与预期不符，这很可能是一个 bug，若${JamConfig.name}无法正常运作，请联系作者",
        LogLevel.WARNING)
    }

    installResult
  }

  /**
   * 自动升级旧插件
   * （当插件版本比数据库版本大时，执行 upgrade 方法）
   *
   * @param installedPlugins 已安装的插件记录
   * @param exec             异步执行上下文
   * @return 安装结果
   */
  private def autoUpgrade(installedPlugins: Map[String, Tables.PluginsRow])(implicit exec: ExecutionContext): Future[Unit] = async {
    val upgrade = await {
      Future.sequence {
        installedPlugins.flatMap {
          case (packageName, record) =>
            // 若存在的版本高于数据库，则升级
            installers.get(packageName).filter(installer => installer.version > record.version)
              .map(installer => (packageName, record.version, installer))
        }.map { case (packageName, oldVersion, installer) => tryUpgradePlugin(packageName, oldVersion, installer) }
      }
    }

    val upgradeResult = upgrade.filter(_._2.isSuccess).map(it => it._1 -> it._2.get)

    await {
      db.run {
        DBIO.sequence(
          upgradeResult.map {
            case (packageName, plugin) =>
              Plugins.filter(_.`package` === packageName)
                .map(row => (row.name, row.author, row.keywords, row.version, row.installDate))
                .update(plugin.pluginName, plugin.author, plugin.keywords.mkString(","), plugin.version, TimeUtil.currentTimeStamp)
          }
        )
      }
    }
  }

  /**
   * 挂载全部挂载点
   *
   * @param installers 安装器列表
   * @return 挂载组件对象
   */
  private def mountPlugins(installers: Iterable[JamPluginInstaller]): LoadedComponents = {
    val bootTasks: ListBuffer[LifeCycleTask] = ListBuffer.empty
    val shutdownTasks: ListBuffer[LifeCycleTask] = ListBuffer.empty
    val preHandleTasks: ListBuffer[PreHandleTask] = ListBuffer.empty
    val containsModeCommandParsers: ListBuffer[SSDLCommandParser[_, Command[_]]] = ListBuffer.empty
    val regexModeCommandParsers: ListBuffer[SSDLCommandParser[_, Command[_]]] = ListBuffer.empty
    val highOrderModeCommandParsers: ListBuffer[SSDLCommandParser[_, Command[_]]] = ListBuffer.empty
    val cronTaskDefinitions: ListBuffer[TaskDefinition] = ListBuffer.empty
    val masterCommands: ListBuffer[MasterEverywhereCommand] = ListBuffer.empty
    val afterSleepTasks: ListBuffer[LifeCycleTask] = ListBuffer.empty
    installers.flatMap(_.mountPoint).foreach { it =>
      bootTasks ++= it.bootTasks
      shutdownTasks ++= it.bootTasks
      preHandleTasks ++= it.preHandleTasks
      cronTaskDefinitions ++= it.cronTaskDefinitions
      masterCommands ++= it.masterCommands
      afterSleepTasks ++= it.afterSleepTasks
      val parsers = it.commandParsers.groupBy(_.commandMatchType)
      parsers.get(Contains).foreach(containsModeCommandParsers ++= _)
      parsers.get(Regex).foreach(regexModeCommandParsers ++= _)
      parsers.get(HighOrder).foreach(highOrderModeCommandParsers ++= _)
    }
    LoadedComponents(
      bootTasks.result(), shutdownTasks.result(), preHandleTasks.result(), containsModeCommandParsers.result(),
      regexModeCommandParsers.result(), highOrderModeCommandParsers.result(), cronTaskDefinitions.result(),
      masterCommands.result(), afterSleepTasks.result()
    )
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
    logger.log(s"正在安装插件：${installer.pluginName}，作者：${installer.author}，包名：$packageName")
    installer.install().recoverWith(err => {
      MasterUtil.notifyAndLog(s"[${installer.pluginName}] 安装插件出错，正在自动尝试卸载...（包名为：$packageName）",
        LogLevel.ERROR, Some(err))
      installer.uninstall().recover(err => {
        MasterUtil.notifyAndLog(s"[${installer.pluginName}] 插件卸载失败，请删除该插件并可以尝试联系插件作者：${installer.author}，包名为：$packageName",
          LogLevel.ERROR, Some(err))
        Failure(err)
      })
    }).map(it => {
      logger.log(s"插件${installer.pluginName}安装结束！")
      packageName -> it
    })
  }

  /**
   * 尝试升级插件
   *
   * @param packageName 包名
   * @param oldVersion  旧版本号
   * @param installer   安装器
   * @param exec        异步上下文
   * @return 安装结果
   */
  private def tryUpgradePlugin(packageName: String, oldVersion: BigDecimal, installer: JamPluginInstaller)(implicit exec: ExecutionContext): Future[(String, Try[JamPluginInstaller])] = {
    logger.log(s"正在升级插件：${installer.pluginName}，版本：$oldVersion -> ${installer.version}，包名：$packageName")
    installer.upgrade(oldVersion).recover(err => {
      MasterUtil.notifyAndLog(s"[${installer.pluginName}] 插件升级失败，可以尝试联系插件作者：${installer.author}，包名为：$packageName",
        LogLevel.ERROR, Some(err))
      Failure(err)
    }).map(it => {
      logger.log(s"插件${installer.pluginName}升级结束！")
      packageName -> it
    })
  }

  /**
   * 列出全部插件
   *
   * @param exec 异步执行上下文
   * @return 全部插件记录
   */
  def listPlugin()(implicit exec: ExecutionContext): Future[Seq[PluginsRow]] =
    db.run(Plugins.sortBy(_.id.asc).result)

  /**
   * 启用插件
   *
   * @param event     消息对象
   * @param id        插件 ID
   * @param reloadNow 是否立刻重新加载果酱
   * @param exec      异步执行上下文
   */
  def enablePlugin(event: EventMessage, id: Long, reloadNow: Boolean = true)(implicit exec: ExecutionContext): Future[Unit] =
    updatePluginStatus(event, id, isEnabled = true, reloadNow = reloadNow)

  /**
   * 禁用插件
   *
   * @param event     消息对象
   * @param id        插件 ID
   * @param reloadNow 是否立刻重新加载果酱
   * @param exec      异步执行上下文
   */
  def disablePlugin(event: EventMessage, id: Long, reloadNow: Boolean = true)(implicit exec: ExecutionContext): Future[Unit] =
    updatePluginStatus(event, id, isEnabled = false, reloadNow = reloadNow)

  /**
   * 更新插件状态
   *
   * @param event     消息对象
   * @param id        插件 ID
   * @param isEnabled 启用 or 禁用
   * @param reloadNow 是否立刻重新加载果酱
   * @param exec      异步执行上下文
   */
  private def updatePluginStatus(event: EventMessage, id: Long, isEnabled: Boolean, reloadNow: Boolean)(implicit exec: ExecutionContext): Future[Unit] = async {
    val plugin = await(getPluginById(id))
    if (plugin.isDefined) {
      if (isEnabled == plugin.get._1.isEnabled) {
        event.respond(s"插件已经${if (isEnabled) "启用" else "禁用"}了")
      } else {
        val result = await(db.run(Plugins.filter(_.id === id).map(_.isEnabled).update(isEnabled)))
        if (result != 1) {
          logger.error("更新插件状态失败，请检查数据库连接！")
          event.respond("更新插件状态失败，请检查数据库连接！")
        } else {
          event.respond(s"插件已成功${if (isEnabled) "启用" else "禁用"}")
          if (reloadNow) {
            await {
              JamLoader.reload().recover(err => {
                logger.error(s"重新加载过程中出现错误！", err)
                event.respond(s"重新加载过程中出现错误！请检查${JamConfig.name}的日志")
                err
              })
            }
          }
        }
      }
    } else {
      event.respond("指定编号的插件不存在！")
    }
  }


  /**
   * 卸载插件
   *
   * @param event 消息对象
   * @param id    插件 ID
   * @param exec  异步执行上下文
   */
  def uninstallPlugin(event: EventMessage, id: Long)(implicit exec: ExecutionContext): Future[Unit] = async {
    val plugin = await(getPluginById(id))
    plugin match {
      case Some((_, installer)) =>
        disablePlugin(event, id)
        await(installer.uninstall()) match {
          case Failure(exception) =>
            logger.error(s"未能成功卸载，请联系插件的作者：${installer.author}", exception)
            event.respond(s"未能成功卸载，请联系插件的作者：${installer.author}")
          case Success(_) =>
            val result = await(db.run(Plugins.filter(_.id === id).delete))
            if (result != 1) {
              logger.error("插件记录删除失败，请检查数据库连接！")
              event.respond("插件记录删除失败，请检查数据库连接！")
            } else {
              event.respond("插件卸载成功！")
            }
        }
      case None => event.respond("指定编号的插件不存在")
    }
  }

  /**
   * 通过 id 获取插件数据以及安装器
   *
   * @param id   插件 id
   * @param exec 异步执行上下文
   * @return 查询结果
   */
  private def getPluginById(id: Long)(implicit exec: ExecutionContext): Future[Option[(PluginsRow, JamPluginInstaller)]] = async {
    await(db.run(Plugins.filter(_.id === id).result.headOption))
      .flatMap(it => installers.get(it.`package`).map(it -> _))
  }
}
