package o.lartifa.jam.common.util

import cn.hutool.core.lang.UUID

import java.util.concurrent.{ExecutorService, Executors, ScheduledExecutorService, TimeUnit}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * 异步工具
 *
 * Author: sinar
 * 2021/7/4 01:25
 */
object AsyncUtil {
  private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
  private val executorService: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())
  private val waitingQueue: mutable.Map[String, (Double, Runnable)] = mutable.Map.empty

  // 每隔半秒刷新一次等待队列
  scheduler.scheduleAtFixedRate(() => {
    val executed = ListBuffer[String]()
    for ((uuid, (time, runnable)) <- waitingQueue) {
      val remain = time - 0.5
      if (remain <= 0) {
        executorService.submit(runnable)
        executed += uuid
      } else {
        waitingQueue.update(uuid, (remain, runnable))
      }
    }
    waitingQueue --= executed
  }, 0, 500, TimeUnit.MILLISECONDS)

  /**
   * 以半秒为单位的 setTimeout 方法
   *
   * @param runnable 执行函数
   * @param seconds  等待秒数
   * @return 唯一 ID
   */
  def setTimeout(runnable: Runnable, seconds: Double = 0): String = {
    val id = UUID.fastUUID().toString
    this.waitingQueue.addOne((id, seconds -> runnable))
    id
  }

  /**
   * 以半秒为单位的 setTimeout 方法
   *
   * @param runnable 执行函数
   * @param seconds  等待秒数
   * @return 唯一 ID
   */
  def setTimeout(seconds: Double)(runnable: Runnable): String = this.setTimeout(runnable, seconds)

  /**
   * 通过 ID 清除异步任务
   *
   * @param uuid ID
   * @return 是否清除成功（若返回 false 代表任务已经被执行了）
   */
  def clearTimeout(uuid: String): Boolean = this.waitingQueue.remove(uuid).isDefined
}
