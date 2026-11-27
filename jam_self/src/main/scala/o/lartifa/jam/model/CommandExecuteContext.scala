package o.lartifa.jam.model

import o.lartifa.jam.common.config.BotConfig
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.cool.qq.listener.base.ExitCodes
import o.lartifa.jam.cool.qq.listener.base.ExitCodes.ExitCode
import o.lartifa.jam.cool.qq.listener.event.CQEvent
import o.lartifa.jam.onebot.domain.*
import o.lartifa.jam.onebot.protocol.api.OutboundGateway
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
case class CommandExecuteContext(var eventMessage: ChatMessage, vars: DBVarPool,
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
  val msgSender: SpecificSender = SpecificSender(this.chatInfo, eventMessage.senderId)
}

object CommandExecuteContext {
  /**
   * 使用会话消息对象构建执行上下文
   *
   * @param eventMessage 会话消息对象
   * @param exec         异步上下文
   * @return 执行上下文
   */
  def apply(eventMessage: ChatMessage)(implicit exec: ExecutionContext): CommandExecuteContext = {
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
    val outbound = requireOutbound()
    val mockedMessage: ChatMessage = event.chatInfo.chatType match {
      case MessageType.GROUP =>
        GroupMessage(
          postType = MessageType.EVENT,
          messageType = MessageType.EVENT,
          messageId = -1L,
          senderId = event.senderId,
          selfId = BotConfig.qID,
          time = Instant.now.getEpochSecond,
          message = event.name,
          rawMessage = event.name,
          font = 0L,
          subType = Some(MessageType.EVENT),
          groupId = event.chatInfo.chatId,
          sender = SenderInfo(event.senderId),
          outbound = outbound,
        )
      case MessageType.DISCUSS =>
        DiscussMessage(
          postType = MessageType.EVENT,
          messageType = MessageType.EVENT,
          messageId = -1L,
          senderId = event.senderId,
          selfId = BotConfig.qID,
          time = Instant.now.getEpochSecond,
          message = event.name,
          rawMessage = event.name,
          font = 0L,
          subType = Some(MessageType.EVENT),
          discussId = event.chatInfo.chatId,
          sender = SenderInfo(event.senderId),
          outbound = outbound,
        )
      case MessageType.PRIVATE =>
        PrivateMessage(
          postType = MessageType.EVENT,
          messageType = MessageType.EVENT,
          messageId = -1L,
          senderId = event.senderId,
          selfId = BotConfig.qID,
          time = Instant.now.getEpochSecond,
          message = event.name,
          rawMessage = event.name,
          font = 0L,
          subType = Some(MessageType.EVENT),
          sender = SenderInfo(event.senderId),
          outbound = outbound,
        )
      case _ => throw ExecutionException("不支持的消息类型")
    }
    new CommandExecuteContext(
      mockedMessage,
      JamContext.variablePool,
      JamContext.messagePool,
      JamContext.stepPool.get(),
      exec,
      startTime
    )
  }

  private def requireOutbound(): OutboundGateway = {
    val gw = JamContext.outbound.get()
    if (gw == null) throw ExecutionException("出站网关尚未就绪")
    gw
  }
}
