package o.lartifa.jam.plugins.api

import o.lartifa.jam.cool.qq.command.base.MasterEverywhereCommand
import o.lartifa.jam.cool.qq.listener.posthandle.PostHandleTask
import o.lartifa.jam.cool.qq.listener.prehandle.PreHandleTask
import o.lartifa.jam.engine.ssdl.parser.SSDLCommandParser
import o.lartifa.jam.model.tasks.LifeCycleTask
import o.lartifa.jam.pool.CronTaskPool.TaskDefinition

/**
 * 挂载点对象
 *
 * Author: sinar
 * 2020/10/1 00:33
 */
case class MountPoint
(
  /**
   * （挂载点）启动任务
   */
  bootTasks: List[LifeCycleTask] = Nil,

  /**
   * （挂载点）停止前任务
   */
  shutdownTasks: List[LifeCycleTask] = Nil,

  /**
   * （挂载点）预处理任务
   */
  preHandleTasks: List[PreHandleTask] = Nil,

  /**
   * （挂载点）后置任务（指令）
   */
  postHandleTasks: List[PostHandleTask] = Nil,

  /**
   * （挂载点）SSDL 指令解析器
   */
  commandParsers: List[SSDLCommandParser.Parser] = Nil,

  /**
   * （挂载点）定时任务定义
   */
  cronTaskDefinitions: List[TaskDefinition] = Nil,

  /**
   * （挂载点）监护人指令
   */
  masterCommands: List[MasterEverywhereCommand] = Nil,

  /**
   * （挂载点）睡眠后任务
   */
  afterSleepTasks: List[LifeCycleTask] = Nil
)
