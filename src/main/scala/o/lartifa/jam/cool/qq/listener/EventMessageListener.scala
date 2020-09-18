package o.lartifa.jam.cool.qq.listener

import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicReference

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.event.{EventHandler, IcqListener}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.{JamConfig, SystemConfig}
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.cool.qq.listener.asking.Questioner
import o.lartifa.jam.cool.qq.listener.handle.SSDLRuleRunner
import o.lartifa.jam.cool.qq.listener.prehandle.PreHandleTask
import o.lartifa.jam.pool.{JamContext, MessagePool}

import scala.async.Async.{async, await}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * 消息监听器
 *
 * Author: sinar
 * 2020/9/18 18:39
 */
object EventMessageListener extends IcqListener {

  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(EventMessageListener.getClass)

  private val messageRecorder: MessagePool = JamContext.messagePool
  private val preHandleTasks: List[PreHandleTask] = PreHandleTaskInitializer.tasks

  // 决定是否响应前置任务和 SSDL 规则
  private val willResponse: AtomicReference[() => Boolean] = new AtomicReference[() => Boolean](
    createFrequencyFunc(JamConfig.responseFrequency)
  )

  /**
   * 监听消息
   *
   * @param eventMessage 消息对象
   */
  @EventHandler
  def listen(eventMessage: EventMessage): Unit = {
    recordMessage(eventMessage) // 记录消息
      .flatMap(it => if (it) Questioner.tryAnswerer(eventMessage) else Future.successful(false)) // 处理存在的询问
      .flatMap(it => if (it && willResponse.get().apply()) preHandleMessage(eventMessage) else Future.successful(false)) // 处理前置任务
      .foreach(it => if (it) SSDLRuleRunner.executeIfFound(eventMessage)) // 执行 SSDL 规则解析
  }

  /**
   * 记录消息内容
   *
   * @param eventMessage 消息对象
   * @return true：记录成功
   *         记录失败时会直接关闭Bot
   */
  def recordMessage(eventMessage: EventMessage): Future[Boolean] = async {
    val isRecordSuccess = await {
      messageRecorder.recordMessage(eventMessage).recover(error => {
        logger.error(error)
        false
      })
    }
    if (!isRecordSuccess) {
      MasterUtil.notifyMaster(s"%s，记忆系统出现问题，无法记录消息历史，消息内容为：${eventMessage.message}")
      MasterUtil.notifyMaster(s"这将导致消息无法被正确回复，Bot即将关闭...")
      sys.exit(1)
    }
    true
  }

  /**
   * 执行前置任务
   *
   * @param eventMessage 消息对象
   * @return 是否继续执行剩余步骤
   */
  def preHandleMessage(eventMessage: EventMessage): Future[Boolean] = async {
    val result: Iterable[Boolean] = if (SystemConfig.MessageListenerConfig.PreHandleTask.runTaskAsync) {
      await(Future.sequence(preHandleTasks.map(_.execute(eventMessage))))
    } else {
      preHandleTasks.map(it => Await.result(it.execute(eventMessage), Duration.Inf))
    }
    result.forall(it => it)
  }.recover(err => {
    logger.error("前置任务执行出错，将直接执行 SSDL", err)
    true
  })

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
