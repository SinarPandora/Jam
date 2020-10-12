package o.lartifa.jam.engine

import better.files.StringExtensions
import cc.moecraft.logger.format.AnsiColor
import cc.moecraft.logger.{HyLogger, LogLevel}
import o.lartifa.jam.bionic.BehaviorInitializer
import o.lartifa.jam.common.config.{JamConfig, JamPluginConfig, SystemConfig}
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.cool.qq.CoolQQLoader
import o.lartifa.jam.cool.qq.listener.EventMessageListener
import o.lartifa.jam.database.temporary.Memory
import o.lartifa.jam.engine.SSDLParseEngine.{ParseFailResult, ParseSuccessResult}
import o.lartifa.jam.model.patterns.ContentMatcher
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext, Step}
import o.lartifa.jam.plugins.JamPluginLoader
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
   * @return 异步结果
   */
  def init(args: Array[String]): Future[Unit] = async {
    makeSureDirsExist()
    Memory.init(args.contains("--flyway_repair"))
    await(JamPluginLoader.initJamPluginSystems())
    JamContext.cronTaskPool.getAndSet(CronTaskPool().autoRefreshTaskDefinition())
    await(BehaviorInitializer.init())
    await(initSSDL())
    runBootTasks()
    Runtime.getRuntime.addShutdownHook(shutdownHookThread)
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
      await(initSSDL())
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
    SystemConfig.ssdlPath.toFile.createDirectoryIfNotExists(createParents = true)
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
   * 初始化并解析 SSDL
   */
  private def initSSDL(): Future[Unit] = async {
    await(loadSSDL()).foreach(errorMessages =>
      MasterUtil.notifyAndLog(errorMessages.mkString("\n"), LogLevel.ERROR))
  }

  /**
   * 读取 SSDL
   *
   * @return 错误信息
   */
  private def loadSSDL(): Future[Option[List[String]]] = {
    logger.log(s"${AnsiColor.GREEN}SSDL脚本解析开始")
    SSDLParseEngine.load().map(result => {
      val success: Option[Seq[Either[ParseFailResult, ParseSuccessResult]]] = result.get(true)
      val fails: Option[Seq[Either[ParseFailResult, SSDLParseEngine.ParseSuccessResult]]] = result.get(false)
      if (fails.isDefined) handleParseFail(fails.get.flatMap(_.left.toSeq))
      else if (success.isDefined) handleParseResult(success.get.flatMap(_.toSeq))
      else None
    }).recover(e => Some(List(e.getMessage)))
  }

  /**
   * 处理解析失败的结果
   *
   * @param fails 解析失败结果列表
   * @return 错误信息
   */
  def handleParseFail(fails: Seq[ParseFailResult]): Option[List[String]] = Some {
    fails.map {
      case ParseFailResult(lineId, filepath, message) =>
        s"""文件：${filepath.stripPrefix(SystemConfig.ssdlPath)}，行数：$lineId，$message""".stripMargin
    }.toList
  }

  /**
   * 处理解析成功的结果
   *
   * @param success 解析成功结果列表
   * @return 错误信息
   */
  private def handleParseResult(success: Seq[ParseSuccessResult]): Option[List[String]] = {
    val steps = mutable.Map[Long, (Option[String], ChatInfo, Step)]()
    val globalMatchers = ListBuffer[ContentMatcher]()
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
      Some(errorMessage.toList)
    } else {
      // 正则 - 开头 - 结尾 - 等于 - 包含
      JamContext.globalMatchers.getAndSet(sortMatchers(globalMatchers))
      JamContext.customMatchers.getAndSet {
        customMatchers.map {
          case (k, v) =>
            (k, v.map {
              case (k2, v2) => (k2, sortMatchers(v2))
            }.toMap)
        }.toMap
      }
      JamContext.stepPool.getAndSet(StepPool(steps.toMap))
      logger.log(s"${AnsiColor.GREEN}共加载${JamContext.globalMatchers.get().length}条SSDL捕获规则")
      logger.log(s"${AnsiColor.GREEN}共加载${steps.size}条SSDL步骤")
      logger.log(s"${AnsiColor.GREEN}SSDL脚本解析结束")
      None
    }
  }

  /**
   * Matcher 排序
   *
   * @param matchers 无序 Matcher 列表
   * @return 排序后的 Matcher 列表
   */
  private def sortMatchers(matchers: ListBuffer[ContentMatcher]): List[ContentMatcher] = {
    val matcherMap = matchers.groupBy(_.`type`)
    List() ++
      matcherMap.getOrElse(ContentMatcher.EQUALS, List.empty).sortBy(_.stepId) ++
      matcherMap.getOrElse(ContentMatcher.REGEX, List.empty).sortBy(_.stepId) ++
      matcherMap.getOrElse(ContentMatcher.STARTS_WITH, List.empty).sortBy(_.stepId) ++
      matcherMap.getOrElse(ContentMatcher.ENDS_WITH, List.empty).sortBy(_.stepId) ++
      matcherMap.getOrElse(ContentMatcher.CONTAINS, List.empty).sortBy(_.stepId)
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
      context.eventMessage.respond("正在重新解析 SSDL（简易步骤定义语言） 脚本")
      await(loadSSDL()) match {
        case Some(messages) =>
          context.eventMessage.respond("脚本内容存在问题，请确认：")
          messages.sliding(10).foreach(lines => context.eventMessage.respond(lines.mkString("\n")))
        case None =>
          context.eventMessage.respond("Compile Success! 0 Warning, 0 Error")
          context.eventMessage.respond(s"共加载${JamContext.globalMatchers.get().length}条捕获信息")
          context.eventMessage.respond(s"共加载${JamContext.stepPool.get().size}条步骤")
      }

      JamContext.initLock.set(false)
    } else {
      context.eventMessage.respond("重新解析正在进行中，请等待")
    }
  }
}
