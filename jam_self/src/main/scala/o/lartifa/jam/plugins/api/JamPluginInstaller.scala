package o.lartifa.jam.plugins.api

import scala.concurrent.Future

/**
 * Jam 插件装载器
 *
 * Author: sinar
 * 2020/10/1 00:10
 */
abstract class JamPluginInstaller(val name: String) {
  /**
   * 挂载点将自动中的内容会挂载到系统中
   */
  val mountPoint: Option[MountPoint] = None

  /**
   * 安装
   * 该安装操作将在挂载点挂载之前执行
   */
  def install(): Future[Unit]

  /**
   * 卸载
   * 卸载将在挂载内容卸载后执行
   */
  def uninstall(): Future[Unit]
}
