package o.lartifa.jam.cool.qq.listener.event

/**
 * 可提取数据的
 *
 * Author: sinar
 * 2021/7/6 22:22
 */
trait Extractable {
  /**
   * 事件中的数据
   *
   * @return 数据
   */
  val data: Map[String, String]
}
