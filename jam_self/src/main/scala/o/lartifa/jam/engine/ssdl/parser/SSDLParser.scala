package o.lartifa.jam.engine.ssdl.parser

import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.engine.proto.Parser
import o.lartifa.jam.model.patterns.{ContentMatcher, SSDLParseResult}

import scala.util.Try

/**
 * SSDL 基本模式语法解析器
 * Author: sinar
 * 2020/1/2 23:17
 */
object SSDLParser extends Parser {

  /**
   * 解析基础模式
   *
   * @param content 待解析字符串
   * @return 解析结果
   */
  @throws[ParseFailException]
  def parseSSDL(content: String)(implicit parseContext: ParseEngineContext): SSDLParseResult = {
    val processedStr = parseContext.processedStr
    parseMatcher(processedStr, parseContext.stepId) match {
      case Some((matcher, command)) =>
        val executable = LogicStructureParser.parseLogic(command).getOrElse(throw ParseFailException("没有指令内容，或指令内容不正确！"))
        SSDLParseResult(parseContext.stepId, executable, Some(matcher))
      case None =>
        val executable = LogicStructureParser.parseLogic(processedStr).getOrElse(throw ParseFailException("没有指令内容，或指令内容不正确！"))
        SSDLParseResult(parseContext.stepId, executable)
    }
  }

  /**
   * 解析消息内容捕获器
   *
   * @param string  待解析字符串
   * @param stepId  步骤 ID
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseMatcher(string: String, stepId: Long)(implicit context: ParseEngineContext): Option[(ContentMatcher, String)] = {
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
}
