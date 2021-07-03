package o.lartifa.jam.engine.ssdl.parser

import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.model.patterns.ContentMatcher

import scala.util.Try

/**
 * 捕获器解析器
 *
 * Author: sinar
 * 2021/7/3 22:27
 */
object MatcherParser {
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
    Patterns.shellLikeCommandPattern.findFirstMatchIn(string).map(result => {
      val prefixes: String = result.group("prefixes")
      val template = context.getTemplate(result.group("template"))
      (ContentMatcher(stepId, template, prefixes.split("[,，]").toList), result.group("command"))
    })
  }
}
