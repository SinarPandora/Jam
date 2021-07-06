package o.lartifa.jam.model.patterns

import java.util.concurrent.atomic.AtomicReference

/**
 * 捕获器组
 *
 * Author: sinar
 * 2021/7/7 00:25
 */
class MatcherGroup {
  /**
   * 全局范围
   */
  val global: AtomicReference[List[ContentMatcher]] = new AtomicReference[List[ContentMatcher]]()
  /**
   * 群范围
   */
  val globalGroup: AtomicReference[List[ContentMatcher]] = new AtomicReference[List[ContentMatcher]]()
  /**
   * 私聊范围
   */
  val globalPrivate: AtomicReference[List[ContentMatcher]] = new AtomicReference[List[ContentMatcher]]()
  /**
   * 具体会话范围
   */
  val custom: AtomicReference[Map[String, Map[Long, List[ContentMatcher]]]] = new AtomicReference()
}
