package o.lartifa.jam.plugins.api

import o.lartifa.jam.cool.qq.command.base.MasterEverywhereCommand
import o.lartifa.jam.cool.qq.listener.prehandle.PreHandleTask
import o.lartifa.jam.engine.parser.SSDLParser
import o.lartifa.jam.model.tasks.{JamCronTask, LifeCycleTask}

/**
 * 挂载点对象
 *
 * Author: sinar
 * 2020/10/1 00:33
 */
case class MountPoint
(
  bootTasks: List[LifeCycleTask] = List.empty,
  shutdownTasks: List[LifeCycleTask] = List.empty,
  preHandleTasks: List[PreHandleTask] = List.empty,
  ssdlParsers: List[SSDLParser] = List.empty,
  cronTasks: List[JamCronTask] = List.empty,
  masterCommands: List[MasterEverywhereCommand] = List.empty,
  afterSleepTasks: List[JamCronTask] = List.empty
)
