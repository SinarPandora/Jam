package jam.plugins.cool.apis.commands

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.engine.ssdl.parser.{ParseEngineContext, SSDLCommandParser}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 毒鸡汤
 *
 * Author: sinar
 * 2020/11/27 00:11
 */
abstract class AntiMotivationalQuotes extends SSDLCommandParser[AntiMotivationalQuotes](SSDLCommandParser.Contains) with Command[String]

object AntiMotivationalQuotes extends AntiMotivationalQuotes {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(AntiMotivationalQuotes.getClass)

  /**
   * 解析指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  override def parse(string: String, context: ParseEngineContext): Option[AntiMotivationalQuotes] =
    if (string.contains("来一锅毒鸡汤")) Some(this) else None

  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[String] = Future {
    val resp = ujson.read(requests.get("https://www.iowen.cn/jitang/api/").text())
    if (resp("status").num != 1) throw ExecutionException("毒鸡汤 API 暂时不可用")
    val amq = resp("data")("content")("content").str
    reply(amq)
    amq
  }.recover {
    case anyErr =>
      logger.error("毒鸡汤 API 调用失败", anyErr)
      // 回复默认毒鸡汤
      reply("我虽然不能来一场，说走就走的旅行，但我有一个说胖就胖的体质。")
      "毒鸡汤获取失败"
  }
}
