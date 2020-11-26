package jam.plugins.cool.apis.commands

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.engine.parser.{ParseEngineContext, SSDLCommandParser}
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
object AntiMotivationalQuotes extends SSDLCommandParser[AntiMotivationalQuotes.type](SSDLCommandParser.Contains) with Command[String] {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(AntiMotivationalQuotes.getClass)

  /**
   * 解析指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  override def parse(string: String, context: ParseEngineContext): Option[AntiMotivationalQuotes.type] =
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
    if (resp("status").num != 1) throw ExecutionException("毒鸡汤 API 调用失败")
    val amq = resp("data")("content")("content").str
    context.eventMessage.respond(amq)
    amq
  }.recover {
    case anyErr =>
      logger.error(anyErr)
      context.eventMessage.respond("鸡汤被厨师喝掉了...")
      "毒鸡汤获取失败"
  }
}
