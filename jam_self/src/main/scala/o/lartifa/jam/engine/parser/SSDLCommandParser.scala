package o.lartifa.jam.engine.parser

/**
 * SSDL 指令解析器
 *
 * Author: sinar
 * 2020/10/1 00:41
 */
abstract class SSDLCommandParser[T]() extends Parser {
  /**
   * 解析指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  def parse(string: String)(implicit context: ParseEngineContext): Option[T]
}
