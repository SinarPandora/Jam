package o.lartifa.jam.model

import cc.moecraft.icq.event.events.message.{EventGroupMessage, EventMessage, EventPrivateMessage}
import o.lartifa.jam.common.config.BotConfig
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.cool.qq.listener.base.ExitCodes
import o.lartifa.jam.cool.qq.listener.base.ExitCodes.ExitCode
import o.lartifa.jam.cool.qq.listener.event.CQEvent
import o.lartifa.jam.pool.*

import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.ExecutionContext

/**
 * 指令执行上下文
 *
 * Author: sinar
 * 2020/1/3 23:50
 */
case class CommandExecuteContext(eventMessage: EventMessage, vars: DBVarPool,
                                 msgRecords: MessagePool, stepPool: StepPool,
                                 executionContext: ExecutionContext, startTime: Timestamp,
                                 var lastResult: Option[String] = None,
                                 var exitCode: ExitCode = ExitCodes.Finish) {
  // 懒加载的临时变量池
  lazy val tempVars: TempVarPool = new TempVarPool(eventMessage, startTime)(executionContext)

  /**
   * 当前聊天会话的信息
   */
  val chatInfo: ChatInfo = ChatInfo(eventMessage)

  /**
   * 当前聊天的发送者
   */
  val msgSender: SpecificSender = SpecificSender(this.chatInfo, eventMessage.getSenderId)
}

object CommandExecuteContext {
  /**
   * 使用会话消息对象构建执行上下文
   *
   * @param eventMessage 会话消息对象
   * @param exec         异步上下文
   * @return 执行上下文
   */
  def apply(eventMessage: EventMessage)(implicit exec: ExecutionContext): CommandExecuteContext = {
    val startTime = Timestamp.from(Instant.now)
    new CommandExecuteContext(
      eventMessage,
      JamContext.variablePool,
      JamContext.messagePool,
      JamContext.stepPool.get(),
      exec,
      startTime
    )
  }

  /**
   * 使用事件消息对象模拟消息对象并构建执行上下文
   *
   * @param event 事件对象
   * @param exec  异步上下文
   * @return 执行上下文
   */
  def apply(event: CQEvent)(implicit exec: ExecutionContext): CommandExecuteContext = {
    val startTime = Timestamp.from(Instant.now)
    val mockedMessage = event.chatInfo.chatType match {
      case MessageType.GROUP | MessageType.DISCUSS =>
        val mocked = new EventGroupMessage()
        mocked.setGroupId(event.chatInfo.chatId)
        mocked.setSubType(MessageType.EVENT)
        mocked
      case MessageType.PRIVATE =>
        val mocked = new EventPrivateMessage()
        mocked.setSubType(MessageType.EVENT)
        mocked
      case _ => throw ExecutionException("不支持的消息类型")
    }
    mockedMessage.setSenderId(event.senderId)
    mockedMessage.setPostType(MessageType.EVENT)
    mockedMessage.setMessageType(MessageType.EVENT)
    mockedMessage.setSelfId(BotConfig.qID)
    mockedMessage.setBot(JamContext.bot.get())
    mockedMessage.setMessage(event.name)
    mockedMessage.setRawMessage(event.name)
    new CommandExecuteContext(
      mockedMessage,
      JamContext.variablePool,
      JamContext.messagePool,
      JamContext.stepPool.get(),
      exec,
      startTime
    )
  }
}
