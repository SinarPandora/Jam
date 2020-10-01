package o.lartifa.jam.plugins.api

import o.lartifa.jam.cool.qq.command.base.MasterEverywhereCommand
import o.lartifa.jam.cool.qq.listener.prehandle.PreHandleTask
import o.lartifa.jam.engine.parser.SSDLCommandParser
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.model.tasks.{JamCronTask, LifeCycleTask}

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
   * （挂载点）SSDL 指令解析器
   */
  commandParsers: List[SSDLCommandParser[_, Command[_]]] = Nil,

  /**
   * （挂载点）定时任务
   */
  cronTasks: List[JamCronTask] = Nil,

  /**
   * （挂载点）监护人指令
   */
  masterCommands: List[MasterEverywhereCommand] = Nil,

  /**
   * （挂载点）睡眠后任务
   */
  afterSleepTasks: List[LifeCycleTask] = Nil
)
