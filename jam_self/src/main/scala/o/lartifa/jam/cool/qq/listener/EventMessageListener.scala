package o.lartifa.jam.cool.qq.listener

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.event.{EventHandler, IcqListener}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.{JamConfig, SystemConfig}
import o.lartifa.jam.common.util.{MasterUtil, TriBoolValue}
import o.lartifa.jam.cool.qq.listener.asking.Questioner
import o.lartifa.jam.cool.qq.listener.base.ExitCodes
import o.lartifa.jam.cool.qq.listener.handle.SSDLRuleRunner
import o.lartifa.jam.cool.qq.listener.posthandle.PostHandleTask
import o.lartifa.jam.cool.qq.listener.prehandle.PreHandleTask
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.pool.{JamContext, MessagePool}

import java.security.SecureRandom
import scala.annotation.tailrec
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
  private var preHandleTasks: List[PreHandleTask] = PreHandleTaskInitializer.tasks
  private var postHandleTasks: List[PostHandleTask] = PostHandleTaskInitializer.tasks

  // 决定是否响应前置任务和 SSDL 规则
  private var willResponse: () => Boolean = createFrequencyFunc(JamConfig.responseFrequency)

  /**
   * 监听消息
   *
   * @param eventMessage 消息对象
   */
  @EventHandler
  def listen(eventMessage: EventMessage): Unit = {
    if (!JamContext.initLock.get()) { // 在当前没有锁的情况下
      recordMessage(eventMessage) // 记录消息
        .flatMap(it => if (it) Questioner.tryAnswerer(eventMessage) else Future.successful(false)) // 处理存在的询问
        .flatMap(it => if (it && willResponse()) preHandleMessage(eventMessage) else Future.successful(false)) // 处理前置任务
        .foreach(it => if (it) SSDLRuleRunner.executeIfFound(eventMessage) // 执行 SSDL 规则解析
          .flatMap(postHandleMessage(eventMessage, _)) // 执行后置任务
        )

    }
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
   * 执行后置任务
   *
   * @param eventMessage 消息对象
   */
  @tailrec def postHandleMessage(eventMessage: EventMessage, contextOpt: Option[CommandExecuteContext]): Future[Unit] = {
    if (contextOpt.isDefined && contextOpt.get.exitCode == ExitCodes.AsUnMatched) {
      postHandleMessage(eventMessage, None)
    } else {
      val tasks = postHandleTasks.filter(it => it.handleOnProcessed == TriBoolValue.Both ||
        ((it.handleOnProcessed == TriBoolValue.True) == contextOpt.isDefined))
      if (tasks.nonEmpty) {
        val fu = if (SystemConfig.MessageListenerConfig.PostHandleTask.runTaskAsync) {
          Future.sequence(tasks.map(_.execute(eventMessage, contextOpt))).map(_ => ())
        } else {
          Future { tasks.foreach(it => Await.result(it.execute(eventMessage, contextOpt), Duration.Inf)) }
        }
        fu.recover(err => {
          logger.error("后置任务执行出错", err)
        })
      } else Future.unit
    }
  }

  /**
   * 调整回复频率
   *
   * @param frequency 回复频率
   */
  def adjustFrequency(frequency: Int): Unit = {
    this.willResponse = createFrequencyFunc(frequency)
  }

  /**
   * 重新加载前置任务
   */
  def reloadPreHandleTasks(): Unit = {
    this.preHandleTasks = PreHandleTaskInitializer.tasks
  }

  /**
   * 重新加载后置任务
   */
  def reloadPostHandleTasks(): Unit = {
    this.postHandleTasks = PostHandleTaskInitializer.tasks
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
