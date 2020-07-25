package o.lartifa.jam.model.commands

import o.lartifa.jam.model.{CommandExecuteContext, Executable, VarKey}

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 保存指令执行结果
 *
 * Author: sinar
 * 2020/7/18 01:50
 */
case class ThenSaveTo(command: Executable[_], key: VarKey) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    val value = await(command.execute()) match {
      case true => "是"
      case false => "否"
      case () => "没有值"
      case other => other.toString
    }
    key.updateOrElseSet(value)
  }
}
