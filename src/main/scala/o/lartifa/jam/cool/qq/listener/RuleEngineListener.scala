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
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.model.patterns.ContentMatcher
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.pool.{JamContext, MessagePool}

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}

/**
 * 全局消息解析器
 *
 * Author: sinar
 * 2020/1/2 22:20
 */
object RuleEngineListener extends IcqListener {

  private implicit val exec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  private lazy val logger: HyLogger = JamContext.logger.get()

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
  def listen(eventMessage: EventMessage): Unit = {
    messageRecorder.recordMessage(eventMessage).onComplete {
      case Failure(exception) =>
        logger.error(exception)
        MasterUtil.notifyMaster(s"发生严重错误：${eventMessage.message}")
      case Success(isRecordSuccess) =>
        if (!isRecordSuccess) MasterUtil.notifyMaster(s"消息记录失败，消息内容为：${eventMessage.getMessage}")
        if (willResponse.get().apply()) findThenDoStep(eventMessage)
    }
  }

  /**
   * 搜索匹配的步骤并启动
   *
   * @param eventMessage 消息对象
   */
  def findThenDoStep(eventMessage: EventMessage): Unit = {
    val matchCost = new StopWatch()
    matchCost.start()
    val stepId: AtomicReference[Option[Long]] = new AtomicReference[Option[Long]](None)
    val ChatInfo(chatType, chatId) = ChatInfo(eventMessage)

    // 查找匹配的步骤
    if (!JamContext.editLock.get()) {
      val scanList = JamContext.customMatchers.get().getOrElse(chatType, Map()).getOrElse(chatId, List()) ++ JamContext.globalMatchers.get()
      implicit val context: CommandExecuteContext = CommandExecuteContext(eventMessage)
      findMatchedStep(eventMessage.getMessage, scanList).foreach { matcher =>
        stepId.set(Some(matcher.stepId))
        JamContext.stepPool.get().goto(matcher.stepId).recover(exception => {
          logger.error(exception)
          MasterUtil.notifyMaster(s"步骤${stepId.get().get}执行失败！原因：${exception.getMessage}")
        })
      }
    }

    // 输出统计内容
    matchCost.stop()
    stepId.get().foreach { id =>
      val cost = matchCost.getTotalTimeSeconds
      if (cost < 1) logger.log(s"${AnsiColor.GREEN}成功捕获！步骤ID：$id，耗时：小于1s")
      else if (cost < 4) logger.log(s"${AnsiColor.GREEN}成功捕获！步骤ID：$id，耗时：${cost}s")
      else logger.warning(s"${AnsiColor.RED}成功捕获但耗时较长，请考虑对捕获条件进行优化。步骤ID：$id，耗时：${cost}s")
    }
  }

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
