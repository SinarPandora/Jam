package o.lartifa.jam.model

import java.sql.Timestamp
import java.time.Instant

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.pool.{DBVarPool, JamContext, StepPool, TempVarPool}

import scala.concurrent.ExecutionContext

/**
 * 指令执行上下文
 *
 * Author: sinar
 * 2020/1/3 23:50
 */
case class CommandExecuteContext(eventMessage: EventMessage, vars: DBVarPool, tempVars: TempVarPool,
                                 stepPool: StepPool, executionContext: ExecutionContext, startTime: Timestamp,
                                 var lastResult: Option[String] = None) {
  // 日志输出器
  def logger: HyLogger = JamContext.logger.get()

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
      new TempVarPool(eventMessage, startTime),
      JamContext.stepPool.get(),
      exec,
      startTime
    )
  }
}
