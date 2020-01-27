package o.lartifa.jam.model.tasks

import cn.hutool.cron.task.Task

/**
 * 定时任务封装
 * Author: sinar
 * 2020/1/25 13:45 
 */
abstract class JamCronTask(val name: String, val taskId: Option[Long] = None) extends Task
