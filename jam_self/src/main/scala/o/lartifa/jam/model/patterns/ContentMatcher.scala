package o.lartifa.jam.model.patterns

import o.lartifa.jam.common.exception.{ExecutionException, ParseFailException}
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
    val keyword = if (template.isPlainString) template.template
    else Await.result(template.execute(), 3.seconds)
    matcher(string, keyword)
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
        case ContentMatcher.SHELL_LIKE_COMMAND => s"发送“$pattern help”"
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

  case object SHELL_LIKE_COMMAND extends Type

  case class EVENT(eventType: Events.EventType) extends Type

  object Events {
    sealed abstract class EventType(val name: String)
    case object Poke extends EventType("拍一拍")
    case object PokeInGroup extends EventType("群内拍一拍")
    case object MemberInc extends EventType("成员入群")
    case object MemberDec extends EventType("群员退群")
    case object MemberKick extends EventType("群员被踢")
    case object SelfBeKick extends EventType("被踢出群聊")
    case object NewGroupHonor extends EventType("群荣耀变更")
    case object NewLuckDog extends EventType("运气王")
    case object PrivateRecall extends EventType("私聊撤回")
    case object GroupRecall extends EventType("群聊撤回")
    case object GroupFileUpload extends EventType("群文件上传")
  }

  object Constant {
    val REGEX: String = "匹配"
    val EQUALS: String = "内容为"
    val CONTAINS: String = "句中出现"
    val ENDS_WITH: String = "句末出现"
    val STARTS_WITH: String = "句首出现"
    val SHELL_LIKE_COMMAND: String = "带参数指令"
  }

  def apply(stepId: Long, `type`: Type, template: RenderStrTemplate): ContentMatcher = {
    val isMatched: (String, String) => Boolean = (target, keyword) => (`type`: @unchecked) match {
      case ContentMatcher.REGEX =>
        if (template.isPlainString) {
          val regex = template.template.r
          regex.matches(target)
        } else keyword.r.matches(target)
      case ContentMatcher.EQUALS => target == keyword
      case ContentMatcher.CONTAINS => target.contains(keyword)
      case ContentMatcher.ENDS_WITH => target.endsWith(keyword)
      case ContentMatcher.STARTS_WITH => target.startsWith(keyword)
      case ContentMatcher.SHELL_LIKE_COMMAND => throw ParseFailException("解析引擎内部错误：带参数指令解析器应使用专用的方法创建")
    }
    new ContentMatcher(stepId, template, `type`, isMatched)
  }

  def apply(stepId: Long, commandName: RenderStrTemplate, prefixes: List[String]): ContentMatcher = {
    new ContentMatcher(stepId, commandName, ContentMatcher.SHELL_LIKE_COMMAND, (target, keywords) => {
      val content = target.trim
      prefixes.map(_ + keywords).exists(content.startsWith)
    })
  }

}
