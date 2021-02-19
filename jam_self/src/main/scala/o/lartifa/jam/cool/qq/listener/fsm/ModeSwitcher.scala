package o.lartifa.jam.cool.qq.listener.fsm

import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.Future

trait ModeSwitcher {
  /**
   * 当前会话变更模式
   *
   * @param mode    模式
   * @param context 指令执行上下文
   */
  def become(mode: Mode)(implicit context: CommandExecuteContext): Unit = {
    modes.put(context.chatInfo, mode)
  }

  /**
   * 将当前会话的模式移除
   *
   * @param context 指令执行上下文
   */
  def unBecome()(implicit context: CommandExecuteContext): Unit = {
    modes.remove(context.chatInfo)
  }
}
