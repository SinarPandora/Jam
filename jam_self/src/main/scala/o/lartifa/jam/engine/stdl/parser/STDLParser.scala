package o.lartifa.jam.engine.stdl.parser

import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.engine.proto.Parser
import o.lartifa.jam.engine.ssdl.parser.{LogicStructureParser, ParseEngineContext}
import o.lartifa.jam.engine.stdl.ast.DTExpInterpreter

import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

/**
 * 定时任务解析器
 *
 * Author: sinar
 * 2020/7/25 14:26
 */
trait STDLParser extends Parser {
  /**
   * 解析 STDL
   *
   * @param rawDTExp     待解析时间表达式
   * @param rawCommand   待解析指令
   * @param parseContext 解析引擎上下文
   * @return 解析结果
   */
  def parseSTDL(rawDTExp: String, rawCommand: String)(implicit parseContext: ParseEngineContext): STDLParseResult
}

object STDLParser extends STDLParser {

  val stdlPattern: Regex = """当(?<cron>\{.+?})时[,，](?<action>.+)""".r

  /**
   * 解析 STDL
   *
   * @param rawDTExp     待解析时间表达式
   * @param rawCommand   待解析指令
   * @param parseContext 解析引擎上下文
   * @return 解析结果
   */
  override def parseSTDL(rawDTExp: String, rawCommand: String)(implicit parseContext: ParseEngineContext): STDLParseResult = {
    DTExpInterpreter.parseThenInterpret(rawDTExp) match {
      case Failure(exception) => STDLParseResult.Fail(exception)
      case Success(cronExp) =>
        Try(LogicStructureParser.parseLogic(rawCommand)).map {
          case Some(command) => STDLParseResult.Succ(parseContext.stepId, cronExp, command)
          case None => STDLParseResult.Fail(ParseFailException("未找到任何可执行的指令"))
        }.recover(err => STDLParseResult.Fail(err)).get
    }
  }
}
