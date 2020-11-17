package o.lartifa.jam.backend

/**
 * CQ 后端接口
 *
 * Author: sinar
 * 2020/11/17 20:06
 */
trait Backend {
  /**
   * 启动后端
   *
   * @param afterBooted 启动后任务（回调）
   */
  def startAndConnectToBackEnd(afterBooted: () => Unit): Unit
}
