package o.lartifa.jam.engine

import better.files.StringExtensions
import cc.moecraft.icq.PicqBotX
import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.bionic.BehaviorInitializer
import o.lartifa.jam.common.config.{JamConfig, JamPluginConfig, SystemConfig}
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.cool.qq.CoolQQLoader
import o.lartifa.jam.cool.qq.command.MasterCommands
import o.lartifa.jam.cool.qq.listener.{EventMessageListener, SystemEventListener}
import o.lartifa.jam.database.temporary.Memory
import o.lartifa.jam.engine.SXDLParseEngine.{SSDLParseSuccessResult, STDLParseSuccessResult, SXDLParseFailResult, SXDLParseSuccessResult}
import o.lartifa.jam.engine.stdl.ast.DTExpInterpreter.InterpreterResult
import o.lartifa.jam.engine.stdl.parser.STDLParseResult.Succ
import o.lartifa.jam.model.patterns.ContentMatcher
import o.lartifa.jam.model.tasks.SimpleTask
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext, Step}
import o.lartifa.jam.plugins.JamPluginLoader
import o.lartifa.jam.plugins.rss.SubscriptionPool
import o.lartifa.jam.pool.CronTaskPool.TaskDefinition
import o.lartifa.jam.pool.{CronTaskPool, JamContext, StepPool}

import scala.async.Async._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
 * 应用加载器
 *
 * Author: sinar
 * 2020/1/4 23:50
 */
object JamLoader {

  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(JamLoader.getClass)
  private val shutdownHookThread: Thread = new Thread(() => {
    val tasks = JamPluginLoader.loadedComponents.shutdownTasks
    if (tasks.nonEmpty) {
      logger.log(s"[ShutdownTasks] 检测到${JamConfig.name}关闭，正在执行关闭任务...")
      tasks.par.map(it => Try(it()).recover(error =>
        logger.error("[ShutdownTasks] 执行关闭任务时出现错误：", error)
      )).seq
      logger.log(s"[ShutdownTasks] ${JamConfig.name}正在终止")
    }
  })

  /**
   * 加载果酱各组件
   *
   * @param client PicqBotX client
   * @param args   命令行参数
   * @return 异步结果
   */
  def init(client: PicqBotX, args: Array[String]): Future[Unit] = async {
    makeSureDirsExist()
    Memory.init(args.contains("--flyway_repair"))
    await(JamPluginLoader.initJamPluginSystems())
    JamContext.cronTaskPool.getAndSet(CronTaskPool().autoRefreshTaskDefinition())
    await(initSXDL())
    client.getEventManager.registerListeners(EventMessageListener, SystemEventListener)
    client.getCommandManager.registerCommands(MasterCommands.commands: _*)
    Runtime.getRuntime.addShutdownHook(shutdownHookThread)
    SubscriptionPool.init()
    runBootTasks()
    await(BehaviorInitializer.init())
  }

  /**
   * 重新加载
   *
   * @return 异步结果
   */
  def reload(): Future[Unit] = async {
    if (!JamContext.initLock.get()) {
      makeSureDirsExist()
      MasterUtil.notifyAndLog(s"开始重新加载${JamConfig.name}的各个组件")
      EventMessageListener.reloadPreHandleTasks()
      CoolQQLoader.reloadMasterCommands()
      JamContext.cronTaskPool.get().autoRefreshTaskDefinition()
      await(BehaviorInitializer.init())
      await(initSXDL())
      runBootTasks()
      JamContext.initLock.getAndSet(false)
      MasterUtil.notifyAndLog("加载完毕！")
    } else {
      MasterUtil.notifyMaster("重新加载进行中...")
    }
  }

  /**
   * 初始化文件目录结构
   * 确保需要的文件夹存在
   */
  private def makeSureDirsExist(): Unit = {
    // SSDL 文件夹
    SystemConfig.sxdlPath.toFile.createDirectoryIfNotExists(createParents = true)
    // 插件文件夹
    JamPluginConfig.path.toFile.createDirectoryIfNotExists(createParents = true)
  }

  /**
   * 执行启动任务
   */
  private def runBootTasks(): Unit = {
    val tasks = JamPluginLoader.loadedComponents.bootTasks
    if (tasks.nonEmpty) {
      logger.log("[BootTasks] 正在依次执行启动任务")
      tasks.par.map(it => Try(it()).recover(error =>
        logger.error(s"[BootTasks] 执行启动任务时出现错误，${JamConfig.name}可能无法正常运作，" +
          "请查看错误信息并尝试禁用相关插件", error)
      )).seq
      logger.log("[BootTasks] 启动任务执行完成")
    }
  }

  /**
   * 初始化并解析 SXDL
   */
  private def initSXDL(): Future[Unit] = async {
    await(loadSXDL()).foreach(message =>
      MasterUtil.notifyAndLog(message.mkString("\n")))
  }

  /**
   * 读取 SSDL
   *
   * @return 错误信息
   */
  private def loadSXDL(): Future[Option[List[String]]] = {
    logger.log(s"${AnsiColor.GREEN}SSDL&STDL脚本解析开始")
    SXDLParseEngine.load().map(result => {
      val success: Option[Seq[Either[SXDLParseFailResult, SXDLParseSuccessResult]]] = result.get(true)
      val fails: Option[Seq[Either[SXDLParseFailResult, SXDLParseSuccessResult]]] = result.get(false)
      if (fails.isDefined) {
        Some("脚本内容存在问题，请确认：" +: handleParseFail(fails.get.flatMap(_.left.toSeq)).getOrElse(List.empty))
      } else if (success.isDefined) {
        val stepDefs = ListBuffer.empty[SSDLParseSuccessResult]
        val taskDefs = ListBuffer.empty[STDLParseSuccessResult]
        success.get.flatMap(_.toSeq).foreach {
          case x: SSDLParseSuccessResult => stepDefs.addOne(x)
          case x: STDLParseSuccessResult => taskDefs.addOne(x)
        }
        val stepSetupMsg = handleSSDLParseResult(stepDefs.toSeq).getOrElse(List.empty)
        val taskSetupMsg = handleSTDLParseResult(taskDefs.toSeq).getOrElse(List.empty)
        Some(stepSetupMsg ++ taskSetupMsg)
      }
      else None
    }).recover(e => {
      logger.error(e)
      Some("解析引擎出现未知错误：" +: List(e.getMessage))
    })
  }

  /**
   * 处理解析失败的结果
   *
   * @param fails 解析失败结果列表
   * @return 错误信息
   */
  def handleParseFail(fails: Seq[SXDLParseFailResult]): Option[List[String]] = Some {
    fails.map {
      case SXDLParseFailResult(lineId, filepath, message) =>
        s"""文件：${filepath.stripPrefix(SystemConfig.sxdlPath)}，行数：$lineId，$message""".stripMargin
    }.toList
  }

  /**
   * 处理 SSDL 解析成功的结果
   *
   * @param success 解析成功结果列表
   * @return 输出内容
   */
  private def handleSSDLParseResult(success: Seq[SSDLParseSuccessResult]): Option[List[String]] = {
    val steps = mutable.Map[Long, (Option[String], ChatInfo, Step)]()
    val globalMatchers = ListBuffer[ContentMatcher]()
    val globalPrivateMatchers = ListBuffer[ContentMatcher]()
    val globalGroupMatchers = ListBuffer[ContentMatcher]()
    val customMatchers = mutable.Map[String, mutable.Map[Long, ListBuffer[ContentMatcher]]]()
    val errorMessage = mutable.ListBuffer[String]()
    success.map(result => (result.result, result.chatInfo, result.name)).foreach {
      case (result, chatInfo, name) =>
        if (steps.contains(result.id)) {
          errorMessage += s"存在重复的步骤 ID：${result.id}"
        } else {
          steps += result.id -> (name, chatInfo, result.toStep)
          chatInfo match {
            case ChatInfo.None => result.matcher.foreach(globalMatchers.addOne)
            case ChatInfo.Group => result.matcher.foreach(globalGroupMatchers.addOne)
            case ChatInfo.Private => result.matcher.foreach(globalPrivateMatchers.addOne)
            case ChatInfo(chatType, chatId) =>
              result.matcher.foreach(
                customMatchers
                  .getOrElseUpdate(chatType, mutable.Map())
                  .getOrElseUpdate(chatId, ListBuffer())
                  .addOne
              )
          }
        }
    }
    if (errorMessage.nonEmpty) {
      Some("装载步骤时出现错误，请确认：" +: errorMessage.toList)
    } else {
      // 正则 - 开头 - 结尾 - 等于 - 包含
      JamContext.globalMatchers.getAndSet(sortMatchers(globalMatchers))
      JamContext.globalGroupMatchers.getAndSet(sortMatchers(globalGroupMatchers))
      JamContext.globalPrivateMatchers.getAndSet(sortMatchers(globalPrivateMatchers))
      JamContext.customMatchers.getAndSet {
        customMatchers.map {
          case (k, v) =>
            (k, v.map {
              case (k2, v2) => (k2, sortMatchers(v2))
            }.toMap)
        }.toMap
      }
      JamContext.stepPool.getAndSet(StepPool(steps.toMap))
      logger.log(s"${AnsiColor.GREEN}${steps.size}条SSDL脚本已全部成功载入！")
      Some(List(
        "SSDL Compile Success! 0 Warning, 0 Error",
        s"已载入${
          globalMatchers.length +
            globalGroupMatchers.length +
            globalPrivateMatchers.length +
            customMatchers.values.map(_.values.size).sum
        }条SSDL捕获规则与${steps.size}条行为步骤"
      ))
    }
  }

  /**
   * 处理 STDL 解析成功的结果
   *
   * @param success 解析成功结果列表
   * @return 输出内容
   */
  private def handleSTDLParseResult(success: Seq[STDLParseSuccessResult]): Option[List[String]] = {
    val errorMessage = mutable.ListBuffer[String]()
    val taskDefs: mutable.Map[String, TaskDefinition] = mutable.Map.empty
    val instance: ListBuffer[SimpleTask] = ListBuffer.empty
    success.foreach {
      case STDLParseSuccessResult(_, _, Succ(id, InterpreterResult(cronExp, description), executable), chatInfo, name) =>
        val task = SimpleTask(id = id, name = name, cronExp = cronExp, chatInfo = chatInfo, executable = executable)
        if (!taskDefs.contains(task.name)) {
          logger.debug(s"任务：${task.name}已被设置在：$description")
          val taskDef = TaskDefinition(task.name, classOf[SimpleTask], isSingleton = true, simpleTaskInstance = Some(task))
          taskDefs += task.name -> taskDef
          instance += task
        } else {
          errorMessage += s"存在重名的任务：${task.name}"
        }
    }
    JamContext.cronTaskPool.get().refreshSimpleTaskDefinitions(taskDefs.toMap)
    instance.foreach(it => it.setUp(it.cronExp))
    if (errorMessage.nonEmpty) {
      Some(errorMessage.toList)
    } else {
      Some(
        List(
          "STDL Compile Success! 0 Warning, 0 Error",
          s"已载入${instance.length}条计划任务"
        )
      )
    }
  }

  /**
   * Matcher 排序
   * 相等 > 正则 > 开头 > 结尾 > 包含
   * 编号大 > 编号小
   *
   * @param matchers 无序 Matcher 列表
   * @return 排序后的 Matcher 列表
   */
  private def sortMatchers(matchers: ListBuffer[ContentMatcher]): List[ContentMatcher] = {
    val matcherMap = matchers.groupBy(_.`type`)
    List() ++
      matcherMap.getOrElse(ContentMatcher.EQUALS, List.empty).sortBy(_.stepId * -1) ++
      matcherMap.getOrElse(ContentMatcher.REGEX, List.empty).sortBy(_.stepId * -1) ++
      matcherMap.getOrElse(ContentMatcher.STARTS_WITH, List.empty).sortBy(_.stepId * -1) ++
      matcherMap.getOrElse(ContentMatcher.ENDS_WITH, List.empty).sortBy(_.stepId * -1) ++
      matcherMap.getOrElse(ContentMatcher.CONTAINS, List.empty).sortBy(_.stepId * -1)
  }

  /**
   * 重新解析 SSDL
   *
   * @param context 指令上下文
   * @return 异步结果
   */
  def reloadSSDL()(implicit context: CommandExecuteContext): Future[Unit] = async {
    if (!JamContext.initLock.get()) {
      JamContext.initLock.set(true)
      context.eventMessage.respond(
        s"""${
          if (JamConfig.RemoteEditing.enable)
            "λ> 远程编辑已开启，即将从远程仓库获取最新脚本文件...\n"
          else ""
        }λ> 已连接到解析器实例，正在重新解析SXDL（简易定义语言）脚本...""".stripMargin)
      await(loadSXDL()).foreach {
        _.sliding(10, 10).foreach(lines => context.eventMessage.respond(lines.map(it => s"λ> $it").mkString("\n")))
      }
      JamContext.initLock.set(false)
    } else {
      context.eventMessage.respond("重新解析正在进行中，请稍后再试...")
    }
  }
}
