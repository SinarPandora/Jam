package o.lartifa.jam.cool.qq.listener.event

/**
 * 可提取数据的事件
 *
 * Author: sinar
 * 2021/7/6 22:22
 */
trait ExtractableEvent {
  /**
   * 事件中的数据
   *
   * @return 数据
   */
  def data: Map[String, String]
}
