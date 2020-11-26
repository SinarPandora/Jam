package jam.plugins.cool.apis.commands

import o.lartifa.jam.engine.parser.{ParseEngineContext, SSDLCommandParser}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command

import scala.concurrent.{ExecutionContext, Future}

/**
 * 天气
 *
 * Author: sinar
 * 2020/11/27 00:08
 */
object WhatTheWeather extends SSDLCommandParser[WhatTheWeather.type](SSDLCommandParser.Contains) with Command[String] {
  /**
   * 解析指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  override def parse(string: String, context: ParseEngineContext): Option[WhatTheWeather.type] =
    if (string.contains("解析并播报天气")) Some(this) else None

  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[String] = ???
}
