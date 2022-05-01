package jam.plugins.cool.apis.commands

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.JSONConfig.formats
import o.lartifa.jam.cool.qq.listener.asking.{Answerer, Result}
import o.lartifa.jam.engine.ssdl.parser.{ParseEngineContext, SSDLCommandParser}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.pool.JamContext
import org.json4s.JValue
import org.json4s.jackson.JsonMethods.{parse => jParse}

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
    if (string.contains("运行天气预报小程序")) Some(this) else None

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
            val resp = jParse {
              requests.get(s"http://wthrcdn.etouch.cn/weather_mini?city=$city").text()
            }
            if ((resp \ "status").extract[Int] != 1000) {
              reply(s"找不到${city}的天气信息")
              Future.successful(Result.Complete)
            } else {
              val json: JValue = resp \ "data"
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
  private def startAsking(json: JValue)(implicit context: CommandExecuteContext, exec: ExecutionContext): Unit = {
    Answerer.sender ? { ctx =>
      ctx.event.message match {
        case "未来几天" | "未来几日" =>
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
  private def showYesterday(json: JValue)(implicit context: CommandExecuteContext): Unit = Try {
    val yesterday = json \ "yesterday"
    reply {
      s"""${(json \ "city").extract[String]}昨日的天气 < ${(yesterday \ "type").extract[String]} >
         |最高气温：${(yesterday \ "high").extract[String].stripPrefix("高温").trim}
         |最低气温：${(yesterday \ "low").extract[String].stripPrefix("低温").trim}
         |${(yesterday \ "fx").extract[String]}${(yesterday \ "fl").extract[String].stripPrefix("<![CDATA[").stripSuffix("]]>")}
         |${(yesterday \ "date").extract[String]}
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
  private def showForecast(json: JValue)(implicit context: CommandExecuteContext): Unit = Try {
    val forecast = (json \ "forecast").extract[Seq[JValue]]
    if (forecast.sizeIs < 2) {
      reply("没有找到未来几天的天气 :/")
      return
    }
    reply(s"${(json \ "city").extract[String]}近日天气：")
    forecast.drop(1).sliding(2, 2).map(days =>
      days.map(day =>
        s"""${(day \ "date").extract[String]} < ${(day \ "type").extract[String]} >
           |${(day \ "low").extract[String].stripPrefix("低温").trim} ~ ${(day \ "high").extract[String].stripPrefix("高温").trim}
           |${(day \ "fengxiang").extract[String]}${(day \ "fengli").extract[String].stripPrefix("<![CDATA[").stripSuffix("]]>")}
           |------------------""".stripMargin)
        .mkString("\n"))
      .foreach(reply(_))
    reply {
      """可以输入："明天"，"昨天"查看对应的天气
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
  private def showTomorrow(json: JValue)(implicit context: CommandExecuteContext): Unit = Try {
    val forecast = (json \ "forecast").extract[Seq[JValue]]
    val tomorrow = if (forecast.sizeIs < 2) {
      reply("没有找到明天的天气 :/")
      return
    } else forecast(1)
    reply {
      s"""${(json \ "city").extract[String]}明日的天气 < ${(tomorrow \ "type").extract[String]} >
         |最高气温：${(tomorrow \ "high").extract[String].stripPrefix("高温").trim}
         |最低气温：${(tomorrow \ "low").extract[String].stripPrefix("低温").trim}
         |${(tomorrow \ "fengxiang").extract[String]}${(tomorrow \ "fengli").extract[String].stripPrefix("<![CDATA[").stripSuffix("]]>")}
         |${(tomorrow \ "date").extract[String]}
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
  private def showToday(json: JValue)(implicit context: CommandExecuteContext): Unit = Try {
    val today = (json \ "forecast").extract[Seq[JValue]].headOption.getOrElse {
      reply("没有找到今天的天气 :/")
      return
    }
    reply {
      s"""${(json \ "city").extract[String]}今日的天气 < ${(today \ "type").extract[String]} >
         |最高气温：${(today \ "high").extract[String].stripPrefix("高温").trim}
         |最低气温：${(today \ "low").extract[String].stripPrefix("低温").trim}
         |${(today \ "fengxiang").extract[String]}${(today \ "fengli").extract[String].stripPrefix("<![CDATA[").stripSuffix("]]>")}
         |${(today \ "date").extract[String]}
         |${(json \ "ganmao").extract[String]}
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
