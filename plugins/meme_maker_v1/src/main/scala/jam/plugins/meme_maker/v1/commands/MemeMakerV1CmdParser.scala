package jam.plugins.meme_maker.v1.commands

import jam.plugins.meme_maker.v1
import o.lartifa.jam.engine.parser.{ParseEngineContext, SSDLCommandParser}

/**
 * Meme maker 指令解析器
 * Author: sinar
 * 2020/11/21 15:04
 */
object MemeMakerV1CmdParser extends SSDLCommandParser[MemeMakerV1Command.type](SSDLCommandParser.Contains) {

  /**
   * 解析指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  override def parse(string: String, context: ParseEngineContext): Option[MemeMakerV1Command.type] =
    if (string.contains("启动MemeMakerV1")) Some(v1.commands.MemeMakerV1Command) else None
}
