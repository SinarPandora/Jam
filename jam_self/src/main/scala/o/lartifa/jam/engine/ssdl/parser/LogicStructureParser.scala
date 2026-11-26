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
   * 同句同时出现「或」与「且/并」时：先按「或」拆成多路，每路内部再解析「且/并」。
   * 例如：`回复%A%，或回复%B%，并Lambda<X>` → A 或 (B 并且 X)
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseAndOr(string: String)(implicit context: ParseEngineContext): Option[Executable[_]] = {
    val resultOr = LogicStructurePattern.or.findAllIn(string).toList
    if (resultOr.nonEmpty) {
      // 或优先；路上若还有且/并，递归进 parseAndOrAsCommand
      val first = parseAndOrAsCommand(string.splitAt(string.indexOf(resultOr.head))._1)
      val rest = resultOr.map(m => parseAndOrAsCommand(stripLogicPrefix(m, isOr = true)))
      Some(RandomOr(first +: rest))
    } else {
      val resultAnd = LogicStructurePattern.and.findAllIn(string).toList
      if (resultAnd.nonEmpty) {
        val firstCommand = CommandParser.parseCommand(string.splitAt(string.indexOf(resultAnd.head))._1, context)
          .getOrElse(throw ParseFailException("与或逻辑前没有一条可执行的指令"))
        Some(And(firstCommand +: parseCommands(resultAnd)))
      } else {
        CommandParser.parseCommand(string, context)
      }
    }
  }

  /**
   * 去掉「或/且/并」连接词前缀，得到分支正文
   */
  private def stripLogicPrefix(matched: String, isOr: Boolean): String = {
    val prefix = if (isOr) """^[，,]?或""" else """^[，,]?[且并]"""
    matched.replaceFirst(prefix, "")
  }

  /**
   * 将一段（可能含且/并）解析为单条 Command（And 本身也是 Command，可嵌进 RandomOr）
   */
  private def parseAndOrAsCommand(string: String)(implicit context: ParseEngineContext): Command[_] = {
    parseAndOr(string) match {
      case Some(command: Command[_]) => command
      case Some(_) => throw ParseFailException("'且并或'子语句结构不正确")
      case None => throw ParseFailException("'且并或'子语句中包含有无法识别的指令内容")
    }
  }

  /**
   * 批量解析指令（用于纯且/并逻辑）
   *
   * @param results 待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseCommands(results: List[String])(implicit context: ParseEngineContext): List[Command[_]] = {
    results
      .map(m => CommandParser.parseCommand(stripLogicPrefix(m, isOr = false), context)
        .orElse(CommandParser.parseCommand(m, context)))
      .tapEach(opt => if (opt.isEmpty) throw ParseFailException("'且并或'子语句中包含有无法识别的指令内容"))
      .map(_.get)
  }
}
