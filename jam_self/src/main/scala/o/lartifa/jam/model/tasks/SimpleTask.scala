package o.lartifa.jam.model.tasks

import cc.moecraft.icq.event.events.message.{EventGroupMessage, EventMessage, EventPrivateMessage}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.BotConfig
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.cool.qq.listener.BanList
import o.lartifa.jam.model.tasks.SimpleTask.{logger, unsafeMockContext}
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext, Executable}
import o.lartifa.jam.pool.JamContext

import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.*

/**
 * 简单任务
 *
 * Author: sinar
 * 2021/1/3 13:18
 */
class SimpleTask(id: Long, name: String, val cronExp: String, chatInfo: ChatInfo, executable: Executable[?])
  extends JamCronTask(id = id.toString, name = name, chatInfo = chatInfo, onlyOnce = false) {
  /**
   * 执行定时任务内容
   * 模拟执行上下文并执行任务
   *
   * @return 并发占位符
   */
  override def run()(implicit exec: ExecutionContext): Future[Unit] = {
    if (chatInfo.chatId == -1) {
      lazy val friends = JamContext.bot.get().getUserManager.userCache.keySet().asScala
        .map(_.toLong).diff(BanList.user)
        .map(qId => chatInfo.copy(chatId = qId)).toSet
      lazy val groups = JamContext.bot.get().getGroupManager.groupCache.keySet().asScala
        .map(_.toLong).diff(BanList.group)
        .map(qId => chatInfo.copy(chatId = qId)).toSet
      val qIDs: Set[ChatInfo] = chatInfo.chatType match {
        case MessageType.PRIVATE => friends
        case MessageType.GROUP => groups
        case MessageType.NONE => friends ++ groups
        case MessageType.DISCUSS => throw ExecutionException("STDL任务暂不支持讨论组")
      }
      Future.sequence(
        qIDs.map(it => {
          Thread.sleep(2000) // 防止被屏蔽
          executable.execute()(unsafeMockContext(it), exec).recover(err => {
            logger.error(err)
            err
          })
        })
      ).map(_ => ())
    } else {
      executable.execute()(unsafeMockContext(chatInfo), exec).map(_ => ())
    }
  }
}

object SimpleTask {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(SimpleTask.getClass)

  /**
   * 【不安全】设置 mocked 消息事件
   * 兼容 java api，采用 setter 的方式设置事件
   *
   * @param event 被 mocked 消息事件
   * @return 设置好的消息事件
   */
  private def unsafeSetUpMockedEventMessage(event: EventMessage): EventMessage = {
    event.setBot(JamContext.bot.get())
    event.setMessage("STDL定时任务自动触发")
    event.setFont(-1)
    event.setMessageId(-1)
    event.setPostType("TASK")
    event.setSelfId(BotConfig.qID)
    event.setTime(System.currentTimeMillis())
    event
  }

  /**
   * 【不安全】设置 mocked 指令执行上下文
   *
   * @param chatInfo 会话信息
   * @param exec     异步执行上下文
   * @return mocked 指令执行上下文
   */
  private def unsafeMockContext(chatInfo: ChatInfo)(implicit exec: ExecutionContext): CommandExecuteContext = {
    val mockedEvent: EventMessage = chatInfo.chatType match {
      case MessageType.PRIVATE =>
        val event = new EventPrivateMessage()
        event.setSenderId(chatInfo.chatId)
        event.setMessageType(MessageType.PRIVATE)
        unsafeSetUpMockedEventMessage(event)
        event
      case MessageType.GROUP =>
        val event = new EventGroupMessage()
        event.setSenderId(chatInfo.chatId)
        event.setMessageType(MessageType.GROUP)
        unsafeSetUpMockedEventMessage(event)
        event.setGroupId(chatInfo.chatId)
        event.getGroup.refreshInfo()
        event
      case MessageType.DISCUSS => throw ExecutionException("STDL任务暂不支持讨论组")
    }
    new CommandExecuteContext(
      mockedEvent,
      JamContext.variablePool,
      JamContext.messagePool,
      JamContext.stepPool.get(),
      exec,
      Timestamp.from(Instant.now)
    )
  }

  def apply(id: Long, name: Option[String], cronExp: String, chatInfo: ChatInfo, executable: Executable[?]): SimpleTask = {
    new SimpleTask(id, name.getOrElse(id.toString), cronExp, chatInfo, executable)
  }
}
