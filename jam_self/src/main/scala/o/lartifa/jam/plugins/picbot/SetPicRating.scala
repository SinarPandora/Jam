package o.lartifa.jam.plugins.picbot

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command

import scala.concurrent.{ExecutionContext, Future}

/**
 * 设置图片等级指令
 *
 * Author: sinar
 * 2020/7/12 15:00
 */
case class SetPicRating(enableR18: Boolean) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] =
    CONFIG_ALLOWED_R18.updateOrElseSet(enableR18.toString).flatMap { _ =>
      if (enableR18) CONFIG_MODE.updateOrElseSet(ONLY.str).map(_ => ())
      else Future.unit
    }
}
