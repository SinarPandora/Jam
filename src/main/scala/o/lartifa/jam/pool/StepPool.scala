package o.lartifa.jam.pool

import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import cn.hutool.core.date.StopWatch
import o.lartifa.jam.common.exception.ExecuteException
import o.lartifa.jam.model.{CommandExecuteContext, Step}

import scala.concurrent.{ExecutionContext, Future}

/**
 *
 * Author: sinar
 * 2020/1/4 01:18
 */
case class StepPool(private val steps: Map[Long, Step]) {

  private lazy val logger: HyLogger = JamContext.logger.get()

  /**
   * 执行指定步骤
   *
   * @param stepId 步骤 id
   * @param exec   异步执行上下文
   * @return 异步执行结果钩子
   */
  @throws[ExecuteException]
  def goto(stepId: Long)(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = {
    val cost = new StopWatch()
    cost.start()
    this.get(stepId).getOrElse({
      cost.stop()
      throw ExecuteException(s"尝试执行步骤${stepId}时失败，原因：步骤缺失！")
    }).execute().map(_ => {
      cost.stop()
      val time = if (cost.getTotalTimeSeconds < 1) "不到一" else cost.getTotalTimeSeconds.toString
      logger.log(s"${AnsiColor.GREEN}步骤${stepId}执行结束，总计耗时：${time}秒")
    })
  }

  /**
   * 获取指定步骤
   *
   * @param stepId 步骤 id
   * @return 该步骤（Optional）
   */
  def get(stepId: Long): Option[Step] = steps.get(stepId)

  /**
   * 步骤数量
   *
   * @return 数量
   */
  def size: Int = this.steps.size
}
