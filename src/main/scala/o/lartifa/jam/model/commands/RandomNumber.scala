package o.lartifa.jam.model.commands

import java.security.SecureRandom

import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 随机数指令
 * [down, up)
 *
 * Author: sinar
 * 2020/1/4 00:07 
 */
case class RandomNumber(down: Int, up: Int) extends Command[Int] {
  /**
   * 执行指令
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Int] = Future {
    new SecureRandom().nextInt(up - down) + down
  }
}