package o.lartifa.jam.model

import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.pool._

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
                                 var lastResult: Option[String] = None) {
  // 懒加载的临时变量池
  lazy val tempVars: TempVarPool = new TempVarPool(eventMessage, startTime)(executionContext)

  /**
   * 当前聊天会话的信息
   */
  val chatInfo: ChatInfo = ChatInfo(eventMessage)
}

object CommandExecuteContext {
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
}
