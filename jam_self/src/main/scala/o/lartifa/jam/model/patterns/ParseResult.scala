package o.lartifa.jam.model.patterns

import o.lartifa.jam.model.{Executable, Step}

/**
 * 最终解析结果
 *
 * Author: sinar
 * 2020/1/3 21:54
 */
case class ParseResult(id: Long, executable: Executable[_], matcher: Option[ContentMatcher] = None) {
  /**
   * 转换为 Step 对象
   *
   * @return Step 对象
   */
  def toStep: Step = Step(id, executable)
}
