package o.lartifa.jam.model.patterns

import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.RenderStrTemplate
import o.lartifa.jam.model.patterns.ContentMatcher._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * 内容捕获器模式
 *
 * Author: sinar
 * 2020/1/3 22:02
 */
case class ContentMatcher(stepId: Long, template: RenderStrTemplate, `type`: Type, matcher: (String, String) => Boolean) {

  /**
   * 判断是否匹配
   *
   * @param string  待比较字符串
   * @param context 指令执行上下文
   * @param exec    异步执行上下文
   * @return 比对结果
   */
  def isMatched(string: String)(implicit context: CommandExecuteContext, exec: ExecutionContext): Boolean = {
    val keywords = if (template.isPlainString) template.template
    else Await.result(template.execute(), 3.seconds)
    matcher(string, keywords)
  }

  /**
   * 触发方式提示
   *
   * @param context 指令执行上下文
   * @param exec    异步执行上下文
   * @return 提示文本
   */
  def intro(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[String] = {
    template.execute().map { pattern =>
      `type` match {
        case ContentMatcher.EQUALS => s"说“$pattern”"
        case ContentMatcher.CONTAINS => s"说包含“$pattern”的话"
        case ContentMatcher.ENDS_WITH => s"说以“$pattern”结尾的话"
        case ContentMatcher.STARTS_WITH => s"说以“$pattern”开头的话"
        case ContentMatcher.REGEX => s"说匹配“$pattern”的话"
        case other => throw ExecutionException(s"该匹配类型不支持转换为提示：$other")
      }
    }
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

  def apply(stepId: Long, `type`: Type, template: RenderStrTemplate): ContentMatcher = {

    val isMatched: (String, String) => Boolean = (target, keywords) => (`type`: @unchecked) match {
      case ContentMatcher.REGEX =>
        if (template.isPlainString) {
          val regex = template.template.r
          regex.matches(target)
        } else keywords.r.matches(target)
      case ContentMatcher.EQUALS => target == keywords
      case ContentMatcher.CONTAINS => target.contains(keywords)
      case ContentMatcher.ENDS_WITH => target.endsWith(keywords)
      case ContentMatcher.STARTS_WITH => target.startsWith(keywords)
    }
    new ContentMatcher(stepId, template, `type`, isMatched)
  }

}
