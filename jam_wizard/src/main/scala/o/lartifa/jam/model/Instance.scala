package o.lartifa.jam.model

import java.time.LocalDateTime

/**
 * Jam 实例
 *
 * Author: sinar
 * 2021/1/20 23:40
 */
case class Instance
(
  id: Int,
  name: String,
  version: String,
  deployPath: String,
  isRunning: Boolean,
  pid: Option[Int],
  managerIds: List[Int],
  backendType: String,
  lastBootTime: Option[LocalDateTime],
  args: String,
  insertedAt: LocalDateTime,
  updatedAt: LocalDateTime
)

object Instance {
  /**
   * 校验是否可以创建
   *
   * @return 是否合法
   */
  def validateCreate: PartialFunction[Instance, Boolean] = {
    case Instance(_, name, version, deployPath, _, _, _, backendType, _, _, _, _) =>
      name != null && version != null && deployPath != null && backendType != null
  }

  /**
   * 校验是否可以创建
   *
   * @return 是否合法
   */
  def validateUpdate: PartialFunction[Instance, Boolean] = {
    case Instance(_, name, version, deployPath, _, _, managerIds, backendType, _, args, _, _) =>
      name != null && version != null && deployPath != null && managerIds != null &&
        backendType != null && args != null
  }
}
