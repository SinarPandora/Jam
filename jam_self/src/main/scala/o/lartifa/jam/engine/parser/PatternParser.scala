package o.lartifa.jam.engine.parser

import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.model.patterns.{ContentMatcher, ParseResult}

import scala.util.Try

/**
 * SSDL 基本模式语法解析器
 * Author: sinar
 * 2020/1/2 23:17
 */
object PatternParser extends Parser {

  /**
   * 解析基础模式
   *
   * @param content 待解析字符串
   * @param id      行前 Id
   * @return 解析结果
   */
  @throws[ParseFailException]
  def parseBasePattern(content: String, id: String): ParseResult = {
    val stepId = Try(id.toLong).getOrElse(throw ParseFailException("步骤编号过大，过小或不合法"))
    // 为引擎设置上下文
    implicit val context: ParseEngineContext = preprocessStatement(content, stepId)
    val processedStr = context.processedStr
    parseMatcher(processedStr, stepId) match {
      case Some((matcher, command)) =>
        val executable = LogicStructureParser.parseLogic(command).getOrElse(throw ParseFailException("没有指令内容，或指令内容不正确！"))
        ParseResult(stepId, executable, Some(matcher))
      case None =>
        val executable = LogicStructureParser.parseLogic(processedStr).getOrElse(throw ParseFailException("没有指令内容，或指令内容不正确！"))
        ParseResult(stepId, executable)
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

  /**
   * 预处理语句中的变量和模板
   *
   * @param string 待解析字符串
   * @param stepId 步骤 ID
   * @return 解析引擎上下文
   */
  private def preprocessStatement(string: String, stepId: Long): ParseEngineContext = {
    // 1. 找到全部模板，替换为 %{_1}%
    val templates = VarParser.parseTemplates(string).getOrElse(Nil).zipWithIndex.map { case (it, idx) => it -> s"_$idx" }
    val str1 = templates.foldLeft(string) { case (str, (it, idx)) => str.replace(it.source, s"%{$idx}%") }
    // 2. 找到剩余的全部变量，替换掉 {_1}
    val varKeys = VarParser.parseVars(str1).getOrElse(Nil).zipWithIndex.map { case (it, idx) => it -> s"_$idx" }
    val processedStr = varKeys.foldLeft(str1) { case (str, (it, idx)) => str.replace(it.source, s"{$idx}") }
    // 3. 组装解析上下文
    val templateMap = templates.map(it => it._2 -> it._1.command).toMap
    val varKeysMap = varKeys.map(it => it._2 -> it._1.varKey).toMap
    // 4. 去除掉除模板外的全部空格
    ParseEngineContext(stepId, varKeysMap, templateMap, string, processedStr.replaceAll("\\s", ""))
  }
}
