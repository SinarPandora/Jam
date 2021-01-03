package o.lartifa.jam.engine.stdl.parser

import o.lartifa.jam.engine.stdl.ast.DTExpInterpreter.InterpreterResult
import o.lartifa.jam.model.Executable

/**
 * Author: sinar
 * 2021/1/2 17:00
 */
sealed trait STDLParseResult {}

object STDLParseResult {
  case class Succ(id: Long, cronExp: InterpreterResult, executable: Executable[_]) extends STDLParseResult
  case class Fail(e: Throwable) extends STDLParseResult
}
