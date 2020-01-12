package o.lartifa.jam.model.patterns

import o.lartifa.jam.model.patterns.ContentMatcher.Type

import scala.util.matching.Regex

/**
 * 内容捕获器模式
 *
 * Author: sinar
 * 2020/1/3 22:02 
 */
case class ContentMatcher(stepId: Long, `type`: Type, keyword: String, regex: Option[Regex] = None) {

  /**
   * 是否匹配
   */
  val isMatched: String => Boolean = (`type`: @unchecked) match {
    case ContentMatcher.REGEX =>
      val rx = regex.get
      rx.matches
    case ContentMatcher.EQUALS => _ == keyword
    case ContentMatcher.CONTAINS => _.contains(keyword)
    case ContentMatcher.ENDS_WITH => _.endsWith(keyword)
    case ContentMatcher.STARTS_WITH => _.startsWith(keyword)
  }
}

object ContentMatcher {

  sealed class Type

  case object REGEX extends Type

  case object EQUALS extends Type

  case object CONTAINS extends Type

  case object ENDS_WITH extends Type

  case object STARTS_WITH extends Type

  object Constant {
    val REGEX: String = "匹配"
    val EQUALS: String = "内容为"
    val CONTAINS: String = "句中出现"
    val ENDS_WITH: String = "句末出现"
    val STARTS_WITH: String = "句首出现"
  }

}
