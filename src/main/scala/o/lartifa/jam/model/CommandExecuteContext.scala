package o.lartifa.jam.model

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.pool.{JamContext, StepPool, VariablePool}

/**
 * 指令执行上下文
 *
 * Author: sinar
 * 2020/1/3 23:50 
 */
case class CommandExecuteContext(eventMessage: EventMessage, variablePool: VariablePool, stepPool: StepPool) {
  def logger: HyLogger = JamContext.logger.get()
}

object CommandExecuteContext {
  def apply(eventMessage: EventMessage): CommandExecuteContext =
    new CommandExecuteContext(eventMessage, JamContext.variablePool, JamContext.stepPool.get())
}
