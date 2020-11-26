package jam.plugins.cool.apis.commands

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.engine.parser.{ParseEngineContext, SSDLCommandParser}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 彩虹屁
 *
 * Author: sinar
 * 2020/11/27 00:08
 */
abstract class RainbowFart extends SSDLCommandParser[RainbowFart](SSDLCommandParser.Contains) with Command[String]

object RainbowFart extends RainbowFart {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(RainbowFart.getClass)

  /**
   * 解析指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  override def parse(string: String, context: ParseEngineContext): Option[RainbowFart] =
    if (string.contains("放彩虹屁")) Some(this) else None

  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[String] = Future {
    val fart = requests.get("https://chp.shadiao.app/api.php").text()
    respond(fart)
    fart
  }.recover {
    case anyErr =>
      logger.error("彩虹屁API调用失败", anyErr)
      // 回复默认彩虹屁
      respond("我不喜欢这世界，我只喜欢你\uD83D\uDC93！")
      "彩虹屁API调用失败"
  }
}
