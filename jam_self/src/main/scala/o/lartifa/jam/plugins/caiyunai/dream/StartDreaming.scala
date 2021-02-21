package o.lartifa.jam.plugins.caiyunai.dream

import o.lartifa.jam.cool.qq.listener.fsm.{Mode, ModeRtnCode}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command

import scala.concurrent.{ExecutionContext, Future}

/**
 * 启动彩云小梦功能指令
 *
 * Author: sinar
 * 2021/2/20 20:11
 */
object StartDreaming extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = {
    become(new Mode {
      // 初始化
      override def execute()(implicit ctx: CommandExecuteContext, ec: ExecutionContext): Future[ModeRtnCode] = {

        ???
      }
    })
    ???
  }
}
