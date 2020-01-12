package o.lartifa.jam.model.commands

import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 变量删除指令
 *
 * Author: sinar
 * 2020/1/4 00:35 
 */
case class ParamDel(name: String) extends Command[Boolean] {
  /**
   * 执行指令
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Boolean] = {
    val pool = context.variablePool
    pool.delete(name)
  }
}
