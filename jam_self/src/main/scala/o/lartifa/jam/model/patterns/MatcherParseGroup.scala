package o.lartifa.jam.model.patterns

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * 捕获器解析组
 *
 * Author: sinar
 * 2021/7/7 00:25
 */
class MatcherParseGroup {
  /**
   * 全局范围
   */
  val global: ListBuffer[ContentMatcher] = ListBuffer[ContentMatcher]()
  /**
   * 群范围
   */
  val globalGroup: ListBuffer[ContentMatcher] = ListBuffer[ContentMatcher]()
  /**
   * 私聊范围
   */
  val globalPrivate: ListBuffer[ContentMatcher] = ListBuffer[ContentMatcher]()
  /**
   * 具体会话范围
   */
  val custom: mutable.Map[String, mutable.Map[Long, ListBuffer[ContentMatcher]]] = mutable.Map[String, mutable.Map[Long, ListBuffer[ContentMatcher]]]()

  /**
   * 统计捕获组数量
   *
   * @return 数量
   */
  def size(): Int = global.length + globalGroup.length + globalPrivate.length + custom.values.map(_.values.size).sum
}
