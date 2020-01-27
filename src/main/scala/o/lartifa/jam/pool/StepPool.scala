package o.lartifa.jam.pool

import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import cn.hutool.core.date.StopWatch
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext, Step}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/**
 * 步骤池
 *
 * Author: sinar
 * 2020/1/4 01:18
 */
class StepPool(private val steps: Map[Long, Step], private val names: Map[String, Map[ChatInfo, Long]]) {

  private lazy val logger: HyLogger = JamContext.logger.get()

  /**
   * 执行指定步骤
   *
   * @param stepId 步骤 id
   * @param exec   异步执行上下文
   * @return 异步执行结果钩子
   */
  @throws[ExecutionException]
  def goto(stepId: Long)(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = {
    val cost = new StopWatch()
    cost.start()
    this.getById(stepId).getOrElse({
      cost.stop()
      throw ExecutionException(s"尝试执行步骤${stepId}时失败，原因：步骤缺失！")
    }).execute().map(_ => {
      cost.stop()
      val time = if (cost.getTotalTimeSeconds < 1) "不到一" else cost.getTotalTimeSeconds.toString
      logger.log(s"${AnsiColor.GREEN}步骤${stepId}执行结束，总计耗时：${time}秒")
    })
  }

  /**
   * 通过 ID 获取指定步骤
   *
   * @param stepId 步骤 id
   * @return 该步骤（Optional）
   */
  def getById(stepId: Long): Option[Step] = steps.get(stepId)

  /**
   * 通过步骤名获取指定步骤
   *
   * @param stepName 步骤名
   * @return 该步骤（Optional）
   */
  def getByName(stepName: String)(implicit context: CommandExecuteContext = null): Option[Step] = {
    names.get(stepName).map(idPairs => {
      val ids = idPairs.values
      if (ids.sizeIs > 1) {
        if (context != null) {
          idPairs.getOrElse(context.chatInfo, return None)
        } else throw ExecutionException(s"存在相同名称的步骤，且聊天情景缺失，请检查有关步骤：${stepName}的调用位置")
      } else if (ids.sizeIs == 1) {
        ids.head
      } else return None
    }).flatMap(getById)
  }

  /**
   * 步骤数量
   *
   * @return 数量
   */
  def size: Int = this.steps.size
}

object StepPool {
  def apply(steps: Map[Long, (Option[String], ChatInfo, Step)]): StepPool = {
    new StepPool(
      // 步骤映射
      steps.map {
        case (id, (_, _, step)) => id -> step
      },{
        // 步骤名映射
        val names = mutable.Map[String, mutable.Map[ChatInfo, Long]]()
        steps.foreach {
          case (id, (nameOpt, chatInfo, _)) => nameOpt.foreach { name =>
            names.getOrElseUpdate(name, mutable.Map.empty).update(chatInfo, id)
          }
        }
        // Freeze mutable
        names.map {
          case (k, v) => k -> v.map {
            case (k2, v2) => k2 -> v2
          }.toMap
        }.toMap
      }
    )
  }
}
