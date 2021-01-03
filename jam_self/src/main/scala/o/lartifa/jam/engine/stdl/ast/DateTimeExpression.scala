package o.lartifa.jam.engine.stdl.ast

import o.lartifa.jam.engine.stdl.ast.DateTimeExpression.WeekDayName.Name

import scala.util.matching.Regex


/**
 * 时间表达式
 *
 * Author: sinar
 * 2020/12/31 21:26
 */
sealed trait DateTimeExpression {}

/**
 * Cron 时间表达式
 *
 * @param exp 表达式
 * @see https://crontab.guru/ 参考
 */
case class CronExpression(exp: String) extends DateTimeExpression

/**
 * 每周几
 *
 * @param weekday 星期
 * @param time    时间表达式
 */
case class WeekDay(weekday: Name, time: Time) extends DateTimeExpression

/**
 * 每几点几分
 *
 * @param hour   小时
 * @param minute 分钟
 */
case class Time(hour: Int, minute: Int) extends DateTimeExpression

/**
 * 日期
 *
 * @param year  年
 * @param month 月
 * @param day   日
 * @param time  时间表达式
 */
case class Date(year: Int, month: Int, day: Int, time: Time) extends DateTimeExpression

object DateTimeExpression {
  val TODAY_ZERO: Time = Time(hour = 0, minute = 0)

  object WeekDayName extends Enumeration {
    type Name = Value
    val MON, TUE, WED, THU, FRI, SAT, SUN = Value

    /**
     * 转换字符串到星期名称
     *
     * @return 转换结果
     */
    def from: PartialFunction[String, Option[Name]] = {
      case "周一" | "星期一" => Some(MON)
      case "周二" | "星期二" => Some(TUE)
      case "周三" | "星期三" => Some(WED)
      case "周四" | "星期四" => Some(THU)
      case "周五" | "星期五" => Some(FRI)
      case "周六" | "星期六" => Some(SAT)
      case "周日" | "周天" | "星期日" | "星期天" => Some(SUN)
      case _ => None
    }
  }

  private case class ParseParams
  (year: Option[String],
   month: Option[String],
   day: Option[String],
   hour: Option[String],
   minute: Option[String],
   weekday: Option[String])

  def apply(raw: String): Option[DateTimeExpression] = parse(raw)

  private val dtExpPattern: Regex =
    """((每|[0-9]+)年)?((每|[0-9]+)月)?((每|[0-9]+)日)?((周|星期)[一二三四五六日天])?((每|[0-9]+)时)?((每|[0-9]+)分)?""".
      r("year", "month", "day", "weekday", "hour", "minute")

  /**
   * 解析日期时间表达式
   *
   * @param raw 原始字符串
   * @return 解析结果
   */
  private def parse(raw: String): Option[DateTimeExpression] = {
    dtExpPattern.findFirstMatchIn(raw).filterNot(_.source == "").map(result => {
      ParseParams(
        year = Option(result.group("year")).map(_.stripSuffix("年")),
        month = Option(result.group("month")).map(_.stripSuffix("月")),
        day = Option(result.group("day")).map(_.stripSuffix("日")),
        hour = Option(result.group("hour")).map(_.stripSuffix("时")),
        minute = Option(result.group("minute")).map(_.stripSuffix("分")),
        weekday = Option(result.group("weekday"))
      )
    }).flatMap {
      case params@ParseParams(_, _, day, _, _, weekday) =>
        println(params)
        weekday match {
          case Some(_) => parseWeekday(params)
          case None => if (day.isDefined) parseDate(params) else parseTime(params)
        }
    }
  }

  /**
   * 解析每周时间
   *
   * @return 解析参数 :: 解析结果
   */
  private def parseWeekday: PartialFunction[ParseParams, Option[WeekDay]] = {
    case params@ParseParams(_, _, _, _, _, weekday) =>
      val time = parseTime(params)
      WeekDayName.from(weekday.get)
        .filter(_ => time.isDefined)
        .map(name => WeekDay(name, time.get))
  }

  /**
   * 解析日期
   *
   * @return 解析参数 :: 解析结果
   */
  private def parseDate: PartialFunction[ParseParams, Option[Date]] = {
    case params@ParseParams(year, month, day, _, _, _) =>
      val time = parseTime(params).getOrElse(TODAY_ZERO)
      Some(Date(
        year = year.map(it => if (it == "每") -1 else it.toInt).getOrElse(-1),
        month = month.map(it => if (it == "每") -1 else it.toInt).getOrElse(-1),
        day = day.map(it => if (it == "每") -1 else it.toInt).getOrElse(-1),
        time = time)
      )
  }

  /**
   * 解析时间
   *
   * @return 解析参数 :: 解析结果
   */
  private def parseTime: PartialFunction[ParseParams, Option[Time]] = {
    case ParseParams(_, _, _, hour, minute, _) =>
      if (hour.isDefined && minute.isDefined) {
        Some(Time(
          hour = hour.map(it => if (it == "每") -1 else it.toInt).getOrElse(-1),
          minute = minute.map(it => if (it == "每") -1 else it.toInt).getOrElse(-1),
        ))
      } else None
  }
}
