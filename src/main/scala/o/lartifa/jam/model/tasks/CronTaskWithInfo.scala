package o.lartifa.jam.model.tasks

import cn.hutool.core.lang.UUID
import o.lartifa.jam.model.ChatInfo

/**
 * 定时任务 + 信息
 *
 * Author: sinar
 * 2020/1/25 16:20 
 */
case class CronTaskWithInfo(task: JamCronTask, chatInfo: Option[ChatInfo] = None, id: UUID = UUID.randomUUID())
