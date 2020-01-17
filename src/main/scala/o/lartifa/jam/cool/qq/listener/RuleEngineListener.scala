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
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.patterns.ContentMatcher
import o.lartifa.jam.pool.{JamContext, MessagePool}

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

  private val willResponse: () => Boolean = {
    val frequency = JamConfig.responseFrequency
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
        notifyMaster(s"发生严重错误：${eventMessage.message}", eventMessage)
      case Success(isRecordSuccess) =>
        if (!isRecordSuccess) notifyMaster(s"消息记录失败，消息内容为：${eventMessage.getMessage}", eventMessage)
        if (willResponse()) findThenDoStep(eventMessage)
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

    // 查找匹配的步骤
    if (!JamContext.editLock.get()) {
      findMatchedStep(eventMessage.getMessage, JamContext.matchers.get()).foreach { matcher =>
        implicit val context: CommandExecuteContext = CommandExecuteContext(eventMessage)
        stepId.set(Some(matcher.stepId))
        JamContext.stepPool.get().goto(matcher.stepId).recover(exception => {
          logger.error(exception)
          notifyMaster(s"步骤${stepId}执行失败！原因：${exception.getMessage}", eventMessage)
        })
      }
    }

    // 输出统计内容
    matchCost.stop()
    stepId.get().foreach { id =>
      val cost = matchCost.getTotalTimeSeconds
      if (cost < 1) logger.log(s"${AnsiColor.GREEN}成功捕获！步骤ID：$id，耗时：小于1s")
      else if (cost < 4) logger.log(s"${AnsiColor.GREEN}成功捕获！步骤ID：$id，耗时：${cost}s")
      else logger.warning(s"${AnsiColor.RED}成功捕获但耗时较长，请考虑对步骤进行优化。步骤ID：$id，耗时：${cost}s")
    }
  }

  /**
   * 寻找匹配的步骤
   *
   * @param message  消息对象
   * @param matchers 捕获器列表
   * @return 匹配结果
   */
  @scala.annotation.tailrec
  private def findMatchedStep(message: String, matchers: List[ContentMatcher]): Option[ContentMatcher] = {
    matchers match {
      case matcher :: next =>
        if (matcher.isMatched(message)) Some(matcher)
        else findMatchedStep(message, next)
      case Nil => None
    }
  }


  /**
   * 通知管理者
   *
   * @param message      错误信息
   * @param eventMessage 消息内容
   */
  private def notifyMaster(message: String, eventMessage: EventMessage): Unit = {
    eventMessage.getHttpApi.sendPrivateMsg(JamConfig.masterQID, message)
  }
}
