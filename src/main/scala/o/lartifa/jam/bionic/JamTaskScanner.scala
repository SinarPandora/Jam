package o.lartifa.jam.bionic

import o.lartifa.jam.bionic.clock.TaskScanner
import o.lartifa.jam.bionic.task.Task
import org.reflections.Reflections

import scala.jdk.CollectionConverters._

/**
 * 定期任务扫描器
 *
 * Author: sinar
 * 2020/1/11 11:41 
 */
class JamTaskScanner(idea: TaskScanner, deJaVu: TaskScanner, routine: TaskScanner) {
  /**
   * 激活生物钟
   *
   * @return 激活结果
   */
  def activeAll(): Boolean = idea.startLoopScan() && deJaVu.startLoopScan() && routine.startLoopScan()

  /**
   * 停止全部
   *
   * @return 停止结果
   */
  def stopAll(): Boolean = idea.stop() && deJaVu.stop() && routine.stop()

  /**
   * 重启全部
   *
   * @return 重启结果
   */
  def restartAll(): Boolean = idea.restart() && deJaVu.restart() && routine.restart()

  /**
   * 是否全部周期扫描都在运行中
   *
   * @return 结果
   */
  def isAllRunning: Boolean = idea.isRunning && deJaVu.isRunning && routine.isRunning
}

object JamTaskScanner {
  def apply(): JamTaskScanner = {
    val tasks: Map[Task.Interval, List[Task[_]]] = new Reflections("o.lartifa.jam.bionic.task")
      .getSubTypesOf(classOf[Task[_]])
      .asScala
      .map(_.getDeclaredConstructor().newInstance())
      .toList
      .groupBy(_.interval)
    new JamTaskScanner(
      new TaskScanner(tasks.getOrElse(Task.OneMinute, Nil), Task.OneMinute.duration),
      new TaskScanner(tasks.getOrElse(Task.TenMinute, Nil), Task.TenMinute.duration),
      new TaskScanner(tasks.getOrElse(Task.HalfAnHour, Nil), Task.HalfAnHour.duration)
    )
  }
}
