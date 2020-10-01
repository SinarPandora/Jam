package o.lartifa.jam.engine.parser

import o.lartifa.jam.engine.parser.SSDLCommandParser.CommandMatchType
import o.lartifa.jam.model.commands.Command

/**
 * SSDL 指令解析器
 *
 * Author: sinar
 * 2020/10/1 00:41
 */
abstract class SSDLCommandParser[U, T <: Command[U]](val commandMatchType: CommandMatchType) extends Parser {
  /**
   * 解析指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  def parse(string: String, context: ParseEngineContext): Option[T]
}

object SSDLCommandParser {

  sealed trait CommandMatchType

  /**
   * 包含模式：
   * 例如：全局禁言指令
   */
  case object Contains extends CommandMatchType

  /**
   * 高阶指令模式：
   * 例如：询问指令，将指令作为参数传入进行解析
   */
  case object HighOrder extends CommandMatchType

  /**
   * 正则匹配模式（最常用的插件模式）
   * 使用正则表达式匹配是否符合指令内容
   */
  case object Regex extends CommandMatchType

}
