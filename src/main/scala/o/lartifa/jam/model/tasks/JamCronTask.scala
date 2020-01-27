package o.lartifa.jam.model.tasks

import cn.hutool.core.lang.UUID
import cn.hutool.cron.CronUtil
import cn.hutool.cron.task.Task
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.pool.JamContext

/**
 * 定时任务封装
 *
 * Author: sinar
 * 2020/1/25 13:45 
 */
abstract class JamCronTask(val name: String, val chatInfo: ChatInfo = ChatInfo.None, val id: UUID = UUID.randomUUID()) extends Task {

  val idString: String = id.toString(true)

  /**
   * 设置并启动定时任务
   *
   * @param cron 定时表达式
   */
  def setUp(cron: String): Unit = {
    CronUtil.schedule(idString, cron, this)
    JamContext.cronTaskPool.get().add(this)
  }

  /**
   * 取消该定时任务
   */
  def cancel(): Unit = {
    CronUtil.remove(idString)
    JamContext.cronTaskPool.get().remove(this)
  }
}
