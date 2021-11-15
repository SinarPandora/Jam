package o.lartifa.jam.pool

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

/**
 * 线程池
 *
 * Author: sinar
 * 2021/8/14 19:00
 */
object ThreadPools {
  /**
   * 单线程池
   */
  val SINGLE: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newSingleThreadExecutor()
  )

  /**
   * 数据访问线程池
   */
  val DB: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newWorkStealingPool(20)
  )

  /**
   * 网络请求线程池
   */
  val NETWORK: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newWorkStealingPool(20)
  )

  /**
   * IO 操作线程池
   */
  val IO: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newCachedThreadPool()
  )

  /**
   *  定时任务线程池
   *  TODO 将全部定时任务修改为 Actor
   */
  val SCHEDULE_TASK: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newCachedThreadPool()
  )

  /**
   * CPU 操作线程池
   */
  val CPU: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())
  )

  /**
   * 默认池
   */
  val DEFAULT: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newWorkStealingPool(Runtime.getRuntime.availableProcessors() * 2)
  )

  /**
   * Lambda 线程池
   */
  val LAMBDA: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newWorkStealingPool(10)
  )
}
