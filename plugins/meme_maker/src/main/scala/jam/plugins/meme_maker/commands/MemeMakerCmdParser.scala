package jam.plugins.meme_maker.commands

import o.lartifa.jam.engine.ssdl.parser.{ParseEngineContext, SSDLCommandParser}

/**
 * Meme maker 指令解析器
 * Author: sinar
 * 2020/11/21 15:04
 */
object MemeMakerCmdParser extends SSDLCommandParser[MemeMakerCommand.type](SSDLCommandParser.Contains) {

  /**
   * 解析指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  override def parse(string: String, context: ParseEngineContext): Option[MemeMakerCommand.type] =
    if (string.contains("启动MemeMaker")) Some(MemeMakerCommand) else None
}
