package o.lartifa.jam.engine.ssdl.parser

import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.engine.proto.Parser
import o.lartifa.jam.model.Executable
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.model.structure._

import scala.util.Try
import scala.util.matching.Regex.Match

/**
 * 逻辑结构解析器
 *
 * Author: sinar
 * 2020/1/4 19:27
 */
object LogicStructureParser extends Parser {

  import Patterns.LogicStructurePattern

  /**
   * 解析基本逻辑结构
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  def parseLogic(string: String)(implicit context: ParseEngineContext): Option[Executable[_]] = {
    parseLoop(string).orElse(parseIfElse(string))
  }

  /**
   * 解析条件结构
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseIfElse(string: String)(implicit context: ParseEngineContext): Option[Executable[_]] = {
    LogicStructurePattern.`if`.findFirstMatchIn(string).map(resultIf => {
      LogicStructurePattern.`else`.findFirstMatchIn(string) match {
        case Some(resultElse) =>
          val orElseCommand = parseAndOr(resultElse.group("command")).getOrElse(throw ParseFailException("'否则分支'中缺失执行指令或执行指令格式不正确"))
          Some(IfWithElse(parseIf(resultIf), orElseCommand))
        case None =>
          Some(parseIf(resultIf))
      }
    }).getOrElse(parseAndOr(string))
  }

  /**
   * 解析循环结构
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseLoop(string: String)(implicit context: ParseEngineContext): Option[Executable[_]] = {
    LogicStructurePattern.loopPattern.findFirstMatchIn(string).map(result => {
      val times = Try(result.group("times").toInt).getOrElse(throw ParseFailException("循环次数必须是数字"))
      need(times > 0, "循环次数必须大于 0")
      Loop(parseAndOr(result.group("command")).getOrElse(throw ParseFailException("循环中的指令不正确")), times)
    })
  }

  /**
   * 解析 If 条件结构
   *
   * @param result  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseIf(result: Match)(implicit context: ParseEngineContext): If = {
    val condition = ConditionParser.parseCondition(result.group("condition")).getOrElse(throw ParseFailException("缺失条件内容或条件内容不正确"))
    val command = parseAndOr(result.group("command").split("否则").head)
      .getOrElse(throw ParseFailException("缺失执行指令或执行指令格式不正确"))
    If(condition, command)
  }

  /**
   * 解析与或逻辑
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseAndOr(string: String)(implicit context: ParseEngineContext): Option[Executable[_]] = {
    val resultAnd = LogicStructurePattern.and.findAllIn(string).toList
    val resultOr = LogicStructurePattern.or.findAllIn(string).toList
    if (resultAnd.nonEmpty && resultOr.nonEmpty) {
      // 逻辑混乱
      throw ParseFailException("与或逻辑混用导致逻辑混乱，请确保'或'、'且'或'并'在如果/否则后面只出现一种")
    } else if (resultAnd.nonEmpty || resultOr.nonEmpty) {
      if (resultAnd.nonEmpty) {
        // 与逻辑
        val firstCommand = CommandParser.parseCommand(string.splitAt(string.indexOf(resultAnd.head))._1, context)
          .getOrElse(throw ParseFailException("与或逻辑前没有一条可执行的指令"))
        Some(And(firstCommand +: parseCommands(resultAnd)))
      } else {
        // 或逻辑
        val firstCommand = CommandParser.parseCommand(string.splitAt(string.indexOf(resultOr.head))._1, context)
          .getOrElse(throw ParseFailException("与或逻辑前没有一条可执行的指令"))
        Some(RandomOr(firstCommand +: parseCommands(resultOr)))
      }
    } else {
      // 不存在与或逻辑对
      CommandParser.parseCommand(string, context)
    }
  }

  /**
   * 批量解析指令（用于与或逻辑）
   *
   * @param results 待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseCommands(results: List[String])(implicit context: ParseEngineContext): List[Command[_]] = {
    results
      .map(CommandParser.parseCommand(_, context))
      .tapEach(opt => if (opt.isEmpty) throw ParseFailException("'且并或'子语句中包含有无法识别的指令内容"))
      .map(_.get)
  }
}
