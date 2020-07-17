package o.lartifa.jam.model

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.util.CommandParameterUtil
import o.lartifa.jam.pool.{JamContext, StepPool, VariablePool}

import scala.collection.mutable

/**
 * 指令执行上下文
 *
 * Author: sinar
 * 2020/1/3 23:50
 */
case class CommandExecuteContext(eventMessage: EventMessage, variablePool: VariablePool, stepPool: StepPool) {
  // 日志输出器
  def logger: HyLogger = JamContext.logger.get()

  /**
   * 当前聊天会话的信息
   */
  val chatInfo: ChatInfo = ChatInfo(eventMessage)

  private val parametersMap: mutable.Map[String, String] = mutable.Map.empty

  /**
   * 以名字获取当前范围内的指令参数
   */
  val params: String => String = CommandParameterUtil.createGetParamFunc(parametersMap.get, eventMessage)

  /**
   * 添加一对参数
   *
   * @param key 键
   * @param value 值
   */
  def addParam(key: String, value: String): Unit = {
    parametersMap += (key -> value)
    ()
  }

  /**
   * 添加参数列表
   *
   * @param pairs 参数列表
   */
  def addParams(pairs: IterableOnce[(String, String)]): Unit = {
    parametersMap ++= pairs
    ()
  }
}

object CommandExecuteContext {
  def apply(eventMessage: EventMessage): CommandExecuteContext =
    new CommandExecuteContext(eventMessage, JamContext.variablePool, JamContext.stepPool.get())
}
