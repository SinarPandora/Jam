package o.lartifa.jam.plugins.api

import scala.concurrent.Future
import scala.util.Try

/**
 * Jam 插件装载器
 *
 * Author: sinar
 * 2020/10/1 00:10
 */
abstract class JamPluginInstaller {

  /**
   * 插件名
   */
  val pluginName: String

  /**
   * 作者名
   */
  val author: String

  /**
   * 关键词
   */
  val keywords: List[String]

  /**
   * 挂载点将自动中的内容会挂载到系统中
   */
  val mountPoint: Option[MountPoint] = None

  /**
   * 安装
   * 该安装操作插件第一次加载到系统时执行
   * 只执行一次
   */
  def install(): Future[Try[Unit]]

  /**
   * 卸载
   * 卸载将在挂载内容卸载后执行
   */
  def uninstall(): Future[Try[Unit]]
}
