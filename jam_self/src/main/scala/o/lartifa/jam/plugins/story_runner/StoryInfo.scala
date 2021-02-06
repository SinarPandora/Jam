package o.lartifa.jam.plugins.story_runner

/**
 * 故事的基本数据
 *
 * Author: sinar
 * 2021/2/6 16:23
 */
case class StoryInfo
(
  path: String,
  name: String,
  checksum: String,
  keyword: String,
  author: String,
  script: String,
  status: String,
  defaultConfig: StoryConfig
)
