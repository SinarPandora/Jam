package jam.plugins.cool.apis.commands

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.cool.qq.listener.asking.{Answerer, Result}
import o.lartifa.jam.engine.parser.{ParseEngineContext, SSDLCommandParser}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.pool.JamContext
import ujson.Value.Value

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * 天气
 *
 * Author: sinar
 * 2020/11/27 00:08
 */
abstract class WhatTheWeather extends SSDLCommandParser[WhatTheWeather](SSDLCommandParser.Contains) with Command[Unit]

object WhatTheWeather extends WhatTheWeather {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(WhatTheWeather.getClass)

  /**
   * 解析指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  override def parse(string: String, context: ParseEngineContext): Option[WhatTheWeather] =
    if (string.contains("解析并播报天气")) Some(this) else None

  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = Future {
    reply("想查哪里的天气呀（输入退出结束查询）")
    Answerer.sender ? { ctx =>
      ctx.event.message match {
        case "退出" => Future.successful(Result.Complete)
        case city =>
          Try {
            val json = ujson.read {
              requests.get(s"http://wthrcdn.etouch.cn/weather_mini?city=$city").text()
            }
            if (json("status").num != 1000) {
              reply(s"找不到${city}的天气信息")
              Future.successful(Result.Complete)
            } else {
              showToday(json)
              startAsking(json)
              Future.successful(Result.Complete)
            }
          }.recover {
            case exception =>
              logger.error("天气API调用失败", exception)
              reply("查询失败 :/")
              Future.successful(Result.Complete)
          }.get
      }
    }
  }

  /**
   * 天气应用逻辑
   *
   * @param json    返回数据
   * @param context 指令上下文
   * @param exec    异步上下文
   */
  private def startAsking(json: Value)(implicit context: CommandExecuteContext, exec: ExecutionContext): Unit = {
    Answerer.sender ? { ctx =>
      ctx.event.message match {
        case "未来几天" =>
          showForecast(json)
          Future.successful(Result.KeepCountAndContinueAsking)
        case "昨天" | "昨日" =>
          showYesterday(json)
          Future.successful(Result.KeepCountAndContinueAsking)
        case "明天" | "明日" =>
          showTomorrow(json)
          Future.successful(Result.KeepCountAndContinueAsking)
        case _ => Future.successful(Result.Complete)
      }
    }
  }

  /**
   * 显示昨天的天气信息
   *
   * @param json    返回数据
   * @param context 指令上下文
   */
  private def showYesterday(json: Value)(implicit context: CommandExecuteContext): Unit = Try {
    val yesterday = json("yesterday")
    reply {
      s"""${json("city")}昨日的天气 < ${yesterday("type").str} >
         |最高气温：${yesterday("high").str.stripPrefix("高温").trim}
         |最低气温：${yesterday("low").str.stripPrefix("低温").trim}
         |${yesterday("fx").str}${yesterday("fl").str.stripPrefix("<![CDATA[").stripPrefix("]]>")}
         |${yesterday("date").str}
         |------------------
         |可以输入："明天"，"未来几天"查看对应的天气
         |任意其他内容会自动退出""".stripMargin
    }
  }.recover {
    case err =>
      logger.error("昨日天气解析失败", err)
      reply("没有找到今天的天气 :/")
  }

  /**
   * 显示五日天气预报
   *
   * @param json    返回数据
   * @param context 指令上下文
   */
  private def showForecast(json: Value)(implicit context: CommandExecuteContext): Unit = Try {
    val forecast = json("forecast").arr
    if (forecast.sizeIs < 2) {
      reply("没有找到未来几天的天气 :/")
      return
    }
    val text = forecast.map(day =>
      s"""${day("date").str} < ${day("type").str} >
         |${day("low").str.stripPrefix("低温").trim} ~ ${day("high").str.stripPrefix("高温").trim}
         |${day("fengxiang").str}${day("fengli").str.stripPrefix("<![CDATA[").stripPrefix("]]>")}
         |------------------""".stripMargin)
      .mkString("\n")
    reply {
      s"""$text
         |可以输入："明天"，"昨天"查看对应的天气
         |任意其他内容会自动退出""".stripMargin
    }
  }.recover {
    case err =>
      logger.error("未来几天天气解析失败", err)
      reply("没有找到未来几天的天气 :/")
  }

  /**
   * 显示明日天气预报
   *
   * @param json    返回数据
   * @param context 指令上下文
   */
  private def showTomorrow(json: Value)(implicit context: CommandExecuteContext): Unit = Try {
    val forecast = json("forecast").arr
    val tomorrow = if (forecast.sizeIs < 2) {
      reply("没有找到明天的天气 :/")
      return
    } else forecast(1)
    reply {
      s"""${json("city")}明日的天气 < ${tomorrow("type").str} >
         |最高气温：${tomorrow("high").str.stripPrefix("高温").trim}
         |最低气温：${tomorrow("low").str.stripPrefix("低温").trim}
         |${tomorrow("fengxiang").str}${tomorrow("fengli").str.stripPrefix("<![CDATA[").stripPrefix("]]>")}
         |${tomorrow("date").str}
         |------------------
         |可以输入："未来几天"，"昨天"查看对应的天气
         |任意其他内容会自动退出""".stripMargin
    }
  }.recover {
    case err =>
      logger.error("明日天气解析失败", err)
      reply("没有找到明天的天气 :/")
  }

  /**
   * 显示当天天气
   *
   * @param json    返回数据
   * @param context 指令上下文
   */
  private def showToday(json: Value)(implicit context: CommandExecuteContext): Unit = Try {
    val today = json("forecast").arr.headOption.getOrElse {
      reply("没有找到今天的天气 :/")
      return
    }
    reply {
      s"""${json("city")}今日的天气 < ${today("type").str} >
         |最高气温：${today("high").str.stripPrefix("高温").trim}
         |最低气温：${today("low").str.stripPrefix("低温").trim}
         |${today("fengxiang").str}${today("fengli").str.stripPrefix("<![CDATA[").stripPrefix("]]>")}
         |${today("date").str}
         |${json("ganmao").str}
         |------------------
         |可以输入："明天"，"未来几天"，"昨天"查看对应的天气
         |任意其他内容会自动退出""".stripMargin
    }
  }.recover {
    case err =>
      logger.error("今日天气解析失败", err)
      reply("没有找到今天的天气 :/")
  }
}
