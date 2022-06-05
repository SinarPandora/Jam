package o.lartifa.jam.plugins.story_runner

import o.lartifa.jam.common.config.JSONConfig.formats
import org.json4s.jackson.Serialization.{read, write}

/**
 * 故事配置
 *
 * Author: sinar
 * 2021/2/4 19:49
 */
case class StoryConfig
(
  // 每句话之间的停顿（秒）
  breakTime: Double,
  // 显示每个选择过程
  showEachChose: Boolean,
  // 每个人都可以参与
  everyOneCanJoin: Boolean,
  // 自动选择如下选项
  autoPlayStep: List[Int],
  // 无损迁移
  enablePainlessMigration: Boolean
) {
  /**
   * 转换为 JSON 字符串
   *
   * @return JSON 字符串
   */
  def jsonStr: String = write(this)
}

object StoryConfig {

  def apply(breakTime: Double, showEachChose: Boolean, everyOneCanJoin: Boolean, autoPlayStep: List[Int], enablePainlessMigration: Boolean): StoryConfig =
    new StoryConfig(breakTime, showEachChose, everyOneCanJoin, autoPlayStep, enablePainlessMigration)

  def apply(string: String): StoryConfig = read[StoryConfig](string)
}
