package o.lartifa.jam.cool.qq.listener

import java.security.SecureRandom
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.event.{EventHandler, IcqListener}
import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import cn.hutool.core.date.StopWatch
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.common.config.SystemConfig.RuleEngineConfig.PreHandleTask
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.cool.qq.listener.RuleEngineListener.{RuleEngineConfig, logger}
import o.lartifa.jam.cool.qq.listener.prehandle.PreHandleTask
import o.lartifa.jam.model.patterns.ContentMatcher
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.pool.{JamContext, MessagePool}

import scala.annotation.tailrec
import scala.async.Async.{async, await}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

/**
 * 全局消息解析器
 *
 * Author: sinar
 * 2020/1/2 22:20
 */
class RuleEngineListener(config: RuleEngineConfig, preHandleTasks: List[PreHandleTask]) extends IcqListener {

  private implicit val exec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  private val messageRecorder: MessagePool = JamContext.messagePool

  private val willResponse: AtomicReference[() => Boolean] = new AtomicReference[() => Boolean](
    createFrequencyFunc(JamConfig.responseFrequency)
  )

  /**
   * 消息监听
   *
   * @param eventMessage 消息对象
   */
  @EventHandler
  def listen(eventMessage: EventMessage): Unit = async {
    messageRecorder.recordMessage(eventMessage).onComplete {
      case Failure(exception) =>
        logger.error(exception)
        MasterUtil.notifyMaster(s"%s，我的记忆出现了问题，无法记录消息历史，消息内容为：${eventMessage.message}")
      case Success(isRecordSuccess) =>
        if (!isRecordSuccess) MasterUtil.notifyMaster(s"%s，我的记忆出现了问题，无法记录消息历史，消息内容为：${eventMessage.getMessage}")
        if (willResponse.get().apply()) responseMessage(eventMessage).onComplete {
          case Failure(exception) =>
            logger.error("前置任务执行出错，将直接执行 SSDL", exception)
            findThenDoStep(eventMessage)
          case Success(shouldExecuteSSDL) => if (shouldExecuteSSDL) findThenDoStep(eventMessage)
        }
    }
  }

  /**
   * 处理消息
   *
   * @param eventMessage 消息对象
   * @return 执行状态占位符
   */
  private def responseMessage(eventMessage: EventMessage): Future[Boolean] = async {
    val result: Iterable[Boolean] = if (config.runPreHandleTaskAsync) {
      await(Future.sequence(preHandleTasks.map(_.execute(eventMessage))))
    } else {
      preHandleTasks.map(it => Await.result(it.execute(eventMessage), Duration.Inf))
    }
    result.forall(it => it)
  }

  /**
   * 搜索匹配的步骤并启动
   *
   * @param eventMessage 消息对象
   */
  def findThenDoStep(eventMessage: EventMessage): Future[Unit] = if (!JamContext.editLock.get()) {
    // 启动计时器
    val matchCost = new StopWatch()
    matchCost.start()
    // 获取会话信息
    val ChatInfo(chatType, chatId) = ChatInfo(eventMessage)
    // 组合捕获器列表
    val scanList = JamContext.customMatchers.get().getOrElse(chatType, Map()).getOrElse(chatId, List()) ++ JamContext.globalMatchers.get()
    // 组建上下文
    implicit val context: CommandExecuteContext = CommandExecuteContext(eventMessage)
    // 查找步骤
    findMatchedStep(eventMessage.getMessage, scanList).map { matcher =>
      val stepId = matcher.stepId
      // 执行任务
      val ssdlTask = JamContext.stepPool.get().goto(stepId).recover(exception => {
        logger.error(exception)
        MasterUtil.notifyMaster(s"%s，步骤${stepId}执行失败了，原因是：${exception.getMessage}")
      }).flatMap(_ => JamContext.messagePool.recordAPlaceholder(eventMessage, "已捕获并执行一次SSDL")).map(_ => ())
      // 输出捕获信息
      matchCost.stop()
      val cost = matchCost.getTotalTimeSeconds
      if (cost < 1) logger.log(s"${AnsiColor.GREEN}成功捕获！步骤ID：$stepId，耗时：小于1s")
      else if (cost < 4) logger.log(s"${AnsiColor.GREEN}成功捕获！步骤ID：$stepId，耗时：${cost}s")
      else logger.warning(s"${AnsiColor.RED}成功捕获但耗时较长，请考虑对捕获条件进行优化。步骤ID：$stepId，耗时：${cost}s")
      ssdlTask
    }.getOrElse {
      Future.successful(matchCost.stop())
    }
  } else Future.successful(())

  /**
   * 寻找匹配的步骤
   *
   * @param message  消息对象
   * @param matchers 捕获器列表
   * @param context  指令执行上下文
   * @param exec     异步执行上下文
   * @return 匹配结果
   */
  @tailrec private def findMatchedStep(message: String, matchers: List[ContentMatcher])(implicit context: CommandExecuteContext,
                                                                                        exec: ExecutionContext): Option[ContentMatcher] = {
    matchers match {
      case matcher :: next =>
        if (matcher.isMatched(message)) Some(matcher)
        else findMatchedStep(message, next)
      case Nil => None
    }
  }

  /**
   * 调整回复频率
   *
   * @param frequency 回复频率
   */
  def adjustFrequency(frequency: Int): Unit = {
    this.willResponse.getAndSet(createFrequencyFunc(frequency))
  }

  /**
   * 生成回复影响函数
   *
   * @param frequency 回复频率
   * @return 回复影响函数
   */
  private def createFrequencyFunc(frequency: Int): () => Boolean = {
    if (frequency == 100) () => true
    else {
      val random = new SecureRandom()
      () => {
        val will = random.nextInt(100) < frequency
        if (!will) logger.debug("受回复几率影响，消息被忽略")
        will
      }
    }
  }
}


object RuleEngineListener {

  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(RuleEngineListener.getClass)

  case class RuleEngineConfig
  (
    // 是否以异步方式运行消息解析前置任务
    runPreHandleTaskAsync: Boolean = PreHandleTask.runTaskAsync
  )

  object RuleEngineConfig {
    val fromConfigFile: RuleEngineConfig = RuleEngineConfig()
  }

  def apply(): RuleEngineListener =
    new RuleEngineListener(RuleEngineConfig.fromConfigFile, PreHandleTaskInitializer.tasks)

  def apply(config: RuleEngineConfig): RuleEngineListener =
    new RuleEngineListener(config, PreHandleTaskInitializer.tasks)
}
