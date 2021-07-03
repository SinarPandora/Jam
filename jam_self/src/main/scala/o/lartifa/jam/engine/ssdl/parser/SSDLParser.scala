package o.lartifa.jam.engine.ssdl.parser

import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.engine.proto.Parser
import o.lartifa.jam.model.patterns.SSDLParseResult

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
    MatcherParser.parseMatcher(processedStr, parseContext.stepId) match {
      case Some((matcher, command)) =>
        val executable = LogicStructureParser.parseLogic(command).getOrElse(throw ParseFailException("没有指令内容，或指令内容不正确！"))
        SSDLParseResult(parseContext.stepId, executable, Some(matcher))
      case None =>
        val executable = LogicStructureParser.parseLogic(processedStr).getOrElse(throw ParseFailException("没有指令内容，或指令内容不正确！"))
        SSDLParseResult(parseContext.stepId, executable)
    }
  }
}
