package o.lartifa.jam.engine.ssdl.parser

import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.model.commands.RenderStrTemplate
import o.lartifa.jam.model.patterns.ContentMatcher
import o.lartifa.jam.model.patterns.ContentMatcher.Events

import scala.util.Try

/**
 * 捕获器解析器
 *
 * Author: sinar
 * 2021/7/3 22:27
 */
object MatcherParser {
  private val eventNames: Set[String] = Set("拍一拍", "群内拍一拍", "成员入群", "群员退群", "群员被踢",
    "被踢出群聊", "群荣耀变更", "运气王", "私聊撤回", "群聊撤回", "群文件上传")
  /**
   * 解析捕获器
   *
   * @param string  待解析字符串
   * @param stepId  步骤 ID
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  def parseMatcher(string: String, stepId: Long)(implicit context: ParseEngineContext): Option[(ContentMatcher, String)] = {
    if (string.startsWith("注册前缀为")) parseCommandMatcher(string, stepId)
    else if (string.startsWith("当接收到事件")) parseEventMatcher(string, stepId)
    else parseMessageMatcher(string, stepId)
  }

  /**
   * 解析消息内容捕获器
   *
   * @param string  待解析字符串
   * @param stepId  步骤 ID
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  def parseMessageMatcher(string: String, stepId: Long)(implicit context: ParseEngineContext): Option[(ContentMatcher, String)] = {
    import ContentMatcher.Constant
    Patterns.matcherPattern.findFirstMatchIn(string).map(result => {
      val `type`: ContentMatcher.Type = result.group("type") match {
        case Constant.EQUALS => ContentMatcher.EQUALS
        case Constant.CONTAINS => ContentMatcher.CONTAINS
        case Constant.ENDS_WITH => ContentMatcher.ENDS_WITH
        case Constant.STARTS_WITH => ContentMatcher.STARTS_WITH
        case s if s.startsWith(Constant.REGEX) => ContentMatcher.REGEX
      }
      val template = context.getTemplate(result.group("template"))
      if (`type` == ContentMatcher.REGEX) {
        Try(template.template.r.matches("TEST")).getOrElse(throw ParseFailException("提供的正则表达式不正确"))
      }
      (ContentMatcher(stepId, `type`, template), result.group("command"))
    })
  }

  /**
   * 解析带参数指令捕获器
   *
   * @param string  待解析字符串
   * @param stepId  步骤 ID
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  def parseCommandMatcher(string: String, stepId: Long)(implicit context: ParseEngineContext): Option[(ContentMatcher, String)] = {
    Patterns.commandMatcherPattern.findFirstMatchIn(string).map(result => {
      val prefixes: String = result.group("prefixes")
      val template = context.getTemplate(result.group("template"))
      (ContentMatcher(stepId, template, prefixes.split("[,，]").toList), result.group("command"))
    })
  }

  /**
   * 解析事件捕获器
   *
   * @param string  待解析字符串
   * @param stepId  步骤 ID
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  def parseEventMatcher(string: String, stepId: Long)(implicit context: ParseEngineContext): Option[(ContentMatcher, String)] = {
    Patterns.eventMatcherPattern.findFirstMatchIn(string).map(result => {
      val eventName = result.group("event")
      if (!eventNames.contains(eventName)) {
        throw ParseFailException(s"绑定了不支持的事件类型：$eventName")
      }
      // 所有支持的事件
      val eventType = eventName match {
        case Events.Poke.name => Events.Poke
        case Events.PokeInGroup.name => Events.PokeInGroup
        case Events.MemberInc.name => Events.MemberInc
        case Events.MemberDec.name => Events.MemberDec
        case Events.MemberKick.name => Events.MemberKick
        case Events.SelfBeKick.name => Events.SelfBeKick
        case Events.NewGroupHonor.name => Events.NewGroupHonor
        case Events.NewLuckDog.name => Events.NewLuckDog
        case Events.PrivateRecall.name => Events.PrivateRecall
        case Events.GroupRecall.name => Events.GroupRecall
        case Events.GroupFileUpload.name => Events.GroupFileUpload
      }
      (ContentMatcher(stepId, ContentMatcher.EVENT(eventType), RenderStrTemplate.Empty), result.group("command"))
    })
  }
}
