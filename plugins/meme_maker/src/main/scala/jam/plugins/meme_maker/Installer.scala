package jam.plugins.meme_maker

import o.lartifa.jam.plugins.api.{JamPluginInstaller, MountPoint}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

/**
 * 插件挂载器
 *
 * Author: sinar
 * 2020/11/17 23:44
 */
class Installer extends JamPluginInstaller {
  /**
   * 插件名
   */
  override val pluginName: String = "表情生成器"
  /**
   * 作者名
   */
  override val author: String = "喵君"
  /**
   * 关键词
   */
  override val keywords: List[String] = List("娱乐", "Gif", "生成", "API")
  /**
   * 版本号
   *
   * 版本号更新时会触发 upgrade 方法
   */
  override val version: BigDecimal = 0.1
  /**
   * 挂载点
   *
   * 挂载点将自动中的内容会挂载到系统中
   */
  override val mountPoint: Option[MountPoint] = Some(
    MountPoint(
      commandParsers = List()
    )
  )

  /**
   * 安装
   * 该安装操作插件第一次加载到系统时执行
   * 只执行一次
   *
   * @param exec 异步上下文
   * @return 安装结果
   */
  override def install()(implicit exec: ExecutionContext): Future[Try[Unit]] =
    Future.successful(Success(()))

  /**
   * 卸载
   * 卸载将在挂载内容卸载后执行
   *
   * @param exec 异步上下文
   * @return 卸载结果
   */
  override def uninstall()(implicit exec: ExecutionContext): Future[Try[Unit]] =
    Future.successful(Success(()))

  /**
   * 更新
   * 更新将在版本号变更（增大）时进行
   *
   * @param exec       异步上下文
   * @param oldVersion 旧版本号
   * @return 安装器本身
   */
  override def upgrade(oldVersion: BigDecimal)(implicit exec: ExecutionContext): Future[Try[JamPluginInstaller]] =
    Future.successful(Success(this))
}
