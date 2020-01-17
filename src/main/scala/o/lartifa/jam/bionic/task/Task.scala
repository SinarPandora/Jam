package o.lartifa.jam.bionic.task

import o.lartifa.jam.bionic.task.Task.Interval

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 周期任务原型
 *
 * Author: sinar
 * 2020/1/17 23:13 
 */
abstract class Task[T](val interval: Interval) {
  /**
   * 执行
   *
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  def execute()(implicit exec: ExecutionContext): Future[T]
}

object Task {
  sealed class Interval(val duration: Duration)

  case object OneMinute extends Interval(1.minute)
  case object TenMinute extends Interval(10.minutes)
  case object HalfAnHour extends Interval(0.5.hour)
}
