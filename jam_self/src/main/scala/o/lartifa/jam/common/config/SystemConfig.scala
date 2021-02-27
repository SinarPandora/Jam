package o.lartifa.jam.common.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.jdk.CollectionConverters._

/**
 * 系统配置
 *
 * Author: sinar
 * 2020/1/4 22:46
 */
object SystemConfig {
  private val config: Config = ConfigFactory.load().getConfig("system")

  // SXDL 脚本目录
  val sxdlPath: String = config.getString("sxdl_path")
  // SXDL 脚本扩展名列表
  val sxdlFileExtension: List[String] = config.getStringList("file_extension").asScala.toList
  // Debug 模式开启标识
  val debugMode: Boolean = config.getBoolean("debugMode")
  // 自动清理消息天数
  val cleanUpMessagePeriod: Int = config.getInt("auto_remove_message_before")

  object MessageListenerConfig {
    private val ruleEngineConfig: Config = config.getConfig("message_listener")
    object PreHandleTask {
      private val preHandleTaskConfig: Config = ruleEngineConfig.getConfig("pre_handle")
      // 允许异步执行前置任务
      val runTaskAsync: Boolean = preHandleTaskConfig.getBoolean("run_task_async")
      // 开启的前置任务
      val enabledTasks: List[String] = preHandleTaskConfig.getStringList("enabled_tasks").asScala.toList
    }
  }
}
