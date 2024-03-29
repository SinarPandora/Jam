package o.lartifa.jam.cool.qq.listener

import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.common.config.PluginConfig
import o.lartifa.jam.cool.qq.listener.posthandle.{AssociatedReply, AssociatedReplyAll, PostHandleTask}
import o.lartifa.jam.plugins.JamPluginLoader
import o.lartifa.jam.pool.JamContext

/**
 * 后置任务初始化器
 *
 * Author: sinar
 * 2021/6/12 19:53
 */
object PostHandleTaskInitializer {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(PostHandleTaskInitializer.getClass)

  def enableTasks: List[String] = PluginConfig.config.postHandle.enabledTasks

  /**
   * 获取当前启用的全部后置任务
   *
   * @return 后置任务
   */
  def tasks: List[PostHandleTask] = {
    val allTasks = Map(
      "联想回复" -> AssociatedReplyAll,
      "未匹配时联想回复" -> AssociatedReply
    ) ++ JamPluginLoader.loadedComponents.postHandleTasks.map(it => it.name -> it)
    val tasks = enableTasks.flatMap(allTasks.get)
    if (enableTasks.sizeIs == tasks.size) {
      logger.log(s"${AnsiColor.GREEN}已启动如下后置任务：${enableTasks.mkString(", ")}")
    } else {
      logger.warning(s"配置文件中部分后置任务不存在，已启动的后置任务：${tasks.map(_.name).mkString(", ")}")
      logger.warning(s"全部可用任务如下：${allTasks.keys.mkString(", ")}")
    }
    tasks
  }
}
