package o.lartifa.jam.cool.qq.listener

import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.common.config.SystemConfig.RuleEngineConfig.PreHandleTask
import o.lartifa.jam.cool.qq.listener.prehandle.{FlipsRepeatedImage, FuckOffMiniApp, PreHandleTask}
import o.lartifa.jam.pool.JamContext

/**
 * 前置任务初始化器
 * 根据配置文件启用前置任务
 *
 * Author: sinar
 * 2020/8/29 21:38
 */
object PreHandleTaskInitializer {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(PreHandleTaskInitializer.getClass)

  /**
   * 获取当前启用的全部前置任务
   *
   * @return 当前启用的全部前置任务
   */
  def tasks: List[PreHandleTask] = {
    // TODO auto scan
    val allTasks = Map(
      "反向复读图片" -> new FlipsRepeatedImage(),
      "替换小程序跳转" -> new FuckOffMiniApp()
    )
    val tasks = PreHandleTask.enabledTasks.flatMap(allTasks.get)
    if (PreHandleTask.enabledTasks.sizeIs == tasks.size) {
      logger.log(s"${AnsiColor.GREEN}已启动如下前置任务：${PreHandleTask.enabledTasks.mkString(", ")}")
    } else {
      logger.warning(s"配置文件中部分前置任务不存在，已启动的前置任务：${tasks.map(_.name).mkString(", ")}")
      logger.warning(s"全部可用任务如下：${allTasks.keys.mkString(", ")}")
    }
    tasks
  }
}
