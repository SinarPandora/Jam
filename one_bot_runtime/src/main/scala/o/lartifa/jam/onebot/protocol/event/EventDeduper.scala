package o.lartifa.jam.onebot.protocol.event

import scala.collection.mutable

/**
 * 多账号短窗去重（对齐 legacy EventParser.isNew + MAO 2s）。
 *
 * Author: sinar
 * 2026/11/28
 */
object EventDeduper {
  private val TimeWindowSec: Long = 2L
  private val MaxPerBucket: Int = 10

  private val cache =
    new java.util.concurrent.ConcurrentHashMap[String, mutable.ArrayBuffer[(String, Long)]]()

  /**
   * @param kind       事件种类（如 group_poke）
   * @param identifier 会话标记（群号等；私聊可用 userId）
   * @param contentKey 内容指纹
   * @param time       事件 time（秒）
   * @param enabled    是否启用去重（多账号优化开启时为 true）
   */
  def isNew(
    kind: String,
    identifier: String,
    contentKey: String,
    time: Long,
    enabled: Boolean,
  ): Boolean = {
    if (!enabled) return true
    val bucketKey = s"$kind|$identifier"
    val list = cache.computeIfAbsent(bucketKey, _ => mutable.ArrayBuffer.empty)
    list.synchronized {
      val dup = list.exists { case (key, t) =>
        key == contentKey && Math.abs(t - time) < TimeWindowSec
      }
      if (dup) false
      else {
        list += ((contentKey, time))
        while (list.size > MaxPerBucket) list.remove(0)
        true
      }
    }
  }

  /** 测试用。 */
  def clear(): Unit = cache.clear()
}
