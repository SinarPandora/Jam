package o.lartifa.jam.engine.ssdl.parser

import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.model.commands.RenderStrTemplate
import o.lartifa.jam.model.{TemplateVarKey, VarKey}

import scala.util.matching.Regex.Match

/**
 * 解析变量相关
 *
 * Author: sinar
 * 2020/7/18 15:23
 */
object VarParser extends Parser {

  case class VarParseResult(varKey: VarKey, source: String)

  case class StringTemplateResult(command: RenderStrTemplate, source: String)

  /**
   * 解析变量键
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  def parseVarKey(string: String): Option[VarKey] = {
    Patterns.varKeyPattern.findFirstMatchIn(string).map(matchToVarKey).map(_.varKey)
  }

  /**
   * 解析全部变量键（使用 split，用于一个指令中多个变量）
   *
   * @param string    待解析字符串
   * @param separator 默认为中英文逗号
   * @return 解析结果
   */
  def parseVarKeysUseSplit(string: String, separator: String = "[,，]"): Option[List[VarKey]] = {
    val keys = string.split(separator).flatMap(parseVarKey).toList
    if (keys.isEmpty) None
    else Some(keys)
  }

  /**
   * 解析渲染字符串模板指令
   *
   * @param string 待解析字符串
   * @return 渲染字符串模板指令
   */
  def parseRenderStrTemplate(string: String): Option[RenderStrTemplate] = {
    if (string.isEmpty) return None
    // 1. 找到其中全部模板变量
    // 2. 解析模板变量中的子字符串模板

    // 1. 找到其中全部变量
    val vars: Seq[VarParseResult] = parseVars(string).getOrElse(return Some(RenderStrTemplate(string, Seq.empty)))
    // 2. 将变量扣除，位置换成 %s
    val template = vars.foldLeft(string) { case (str, result) => str.replace(result.source, "%s") }
    Some(RenderStrTemplate(template, vars.map(_.varKey)))
  }

  /**
   * 解析参数列表
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  def parseVars(string: String): Option[List[VarParseResult]] = {
    val keys = Patterns.varKeyPattern.findAllMatchIn(string).map(matchToVarKey).toList
    if (keys.isEmpty) None
    else Some(keys)
  }

  /**
   * 解析模板列表
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  def parseTemplates(string: String): Option[List[StringTemplateResult]] = {
    val templates = Patterns.stringTemplatePattern.findAllMatchIn(string)
      .map(x => (x.group("template"), x.matched))
      .map { case (content, source) => StringTemplateResult(
        parseRenderStrTemplate(content.stripMargin).getOrElse(throw ParseFailException("字符串模板格式不正确")), source)
      }
      .toList
    if (templates.isEmpty) None
    else Some(templates)
  }

  /**
   * 将匹配结果转换为变量键
   *
   * @param result 匹配结果
   * @return 变量键
   */
  private def matchToVarKey(result: Match): VarParseResult = {
    val defaultValue = Option(result.group("default")).map(_.stripPrefix("|"))
    result.group("type") match {
      case x if VarKey.Type.temp.contains(x) =>
        VarParseResult(VarKey(result.group("name"), VarKey.Temp, defaultValue), result.matched)
      case x if VarKey.Type.db.contains(x) =>
        VarParseResult(VarKey(result.group("name"), VarKey.DB, defaultValue), result.matched)
      case varType if VarKey.Type.templateVarTemp.contains(varType) =>
        VarParseResult(new TemplateVarKey(VarKey(result.group("name"), VarKey.Temp, defaultValue),
          parseTemplateVarKeySubType(varType.stripPrefix("*"))), result.matched)
      case varType if VarKey.Type.templateVarDB.contains(varType) =>
        VarParseResult(new TemplateVarKey(VarKey(result.group("name"), VarKey.DB, defaultValue),
          parseTemplateVarKeySubType(varType)), result.matched)
    }
  }

  /**
   * 解析模板变量子类型
   *
   * @param varType 变量类型
   * @return 子类型
   */
  private def parseTemplateVarKeySubType(varType: String): TemplateVarKey.TemplateVarSubType = {
    varType match {
      case x if TemplateVarKey.At.prefixes.contains(x) => TemplateVarKey.At
      case x if TemplateVarKey.Image.prefixes.contains(x) => TemplateVarKey.Image
      case x if TemplateVarKey.QQFace.prefixes.contains(x) => TemplateVarKey.QQFace
      case x if TemplateVarKey.Command.prefixes.contains(x) => TemplateVarKey.Command
    }
  }
}
