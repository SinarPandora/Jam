package o.lartifa.jam.plugins.picbot

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 设置图片获取模式指令
 *
 * Author: sinar
 * 2020/7/12 15:02
 */
case class SetPicFetcherMode(mode: Mode) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    await(context.vars.updateOrElseSet(CONFIG_MODE, mode.str))
  }
}
