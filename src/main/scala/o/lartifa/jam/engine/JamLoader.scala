package o.lartifa.jam.engine

import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.bionic.BehaviorInitializer
import o.lartifa.jam.common.config.SystemConfig
import o.lartifa.jam.database.temporary.TemporaryMemory
import o.lartifa.jam.engine.SSDLParseEngine.{ParseFailResult, ParseSuccessResult}
import o.lartifa.jam.model.patterns.ContentMatcher
import o.lartifa.jam.model.{CommandExecuteContext, Step}
import o.lartifa.jam.pool.{JamContext, StepPool}

import scala.async.Async._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * 应用加载器
 *
 * Author: sinar
 * 2020/1/4 23:50 
 */
object JamLoader {

  private lazy val logger: HyLogger = JamContext.logger.get()

  /**
   * 加载 SSDL
   *
   * @return 异步结果
   */
  def init(): Future[Unit] = async {
    await(TemporaryMemory.init())
    await(loadSSDL()).foreach(errorMessages => logger.error(errorMessages.mkString("\n")))
    await(BehaviorInitializer.init())
  }

  /**
   * 读取 SSDL
   *
   * @return 错误信息
   */
  private def loadSSDL(): Future[Option[List[String]]] = async {
    val result = await(SSDLParseEngine.load())
    val success: Option[Seq[Either[ParseFailResult, ParseSuccessResult]]] = result.get(true)
    val fails: Option[Seq[Either[ParseFailResult, SSDLParseEngine.ParseSuccessResult]]] = result.get(false)
    if (fails.isDefined) handleParseFail(fails.get.flatMap(_.swap.toSeq))
    else if (success.isDefined) handleParseResult(success.get.flatMap(_.toSeq))
    else None
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
    val steps = mutable.Map[Long, Step]()
    val matchers = mutable.ListBuffer[ContentMatcher]()
    val errorMessage = mutable.ListBuffer[String]()
    success.map(_.result).foreach { result =>
      if (steps.contains(result.id)) {
        errorMessage += s"存在重复的步骤 ID：${result.id}"
      } else {
        steps += result.id -> result.toStep
        result.matcher.foreach(matchers.addOne)
      }
    }
    if (errorMessage.nonEmpty) {
      Some(errorMessage.toList)
    } else {
      // 正则 - 开头 - 结尾 - 等于 - 包含
      val matcherMap = matchers.groupBy(_.`type`)
      JamContext.matchers.getAndSet {
        List() ++
          matcherMap.getOrElse(ContentMatcher.EQUALS, List.empty) ++
          matcherMap.getOrElse(ContentMatcher.REGEX, List.empty) ++
          matcherMap.getOrElse(ContentMatcher.STARTS_WITH, List.empty) ++
          matcherMap.getOrElse(ContentMatcher.ENDS_WITH, List.empty) ++
          matcherMap.getOrElse(ContentMatcher.CONTAINS, List.empty)
      }
      JamContext.stepPool.getAndSet(StepPool(steps.toMap))
      logger.log(s"${AnsiColor.GREEN}共加载${JamContext.matchers.get().length}条捕获信息")
      logger.log(s"${AnsiColor.GREEN}共加载${steps.size}条步骤")
      None
    }
  }

  /**
   * 重新解析 SSDL
   *
   * @param context 指令上下文
   * @return 异步结果
   */
  def reloadSSDL()(implicit context: CommandExecuteContext): Future[Unit] = async {
    if (!JamContext.editLock.get()) {
      JamContext.editLock.set(true)
      context.eventMessage.respond("正在重新解析 SSDL（简易步骤定义语言） 脚本")
      await(loadSSDL()) match {
        case Some(messages) =>
          context.eventMessage.respond("脚本内容存在问题，请确认：")
          messages.sliding(10).foreach(lines => context.eventMessage.respond(lines.mkString("\n")))
        case None =>
          context.eventMessage.respond("Compile Success! 0 Warning, 0 Error")
          context.eventMessage.respond(s"共加载${JamContext.matchers.get().length}条捕获信息")
          context.eventMessage.respond(s"共加载${JamContext.stepPool.get().size}条步骤")
      }

      JamContext.editLock.set(false)
    } else {
      context.eventMessage.respond("重新解析正在进行中，请等待")
    }
  }
}
