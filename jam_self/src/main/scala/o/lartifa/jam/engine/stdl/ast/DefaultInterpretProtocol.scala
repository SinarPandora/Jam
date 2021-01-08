package o.lartifa.jam.engine.stdl.ast


/**
 * SDExp 解析协议默认实现
 *
 * Author: sinar
 * 2021/1/1 14:30
 */
trait DefaultInterpretProtocol extends InterpretProtocol {
  override implicit val interpretCronExp: Interpret[CronExpression] = _.exp
  override implicit val interpretTime: Interpret[Time] = {
    case DateTimeExpression.TODAY_ZERO => "0 0 * * *"
    case Time(hour, minute) => interpretHAndM(hour, minute) + " * * *"
  }
  override implicit val interpretDate: Interpret[Date] = {
    case Date(year, month, day, Time(hour, minute)) =>
      interpretHAndM(hour, minute) + " " +
        s"${if (day == -1) "*" else day} " +
        s"${if (month == -1) "*" else month} " +
        s"${if (year == -1) "*" else year}"
  }
  override implicit val interpretEachAt: Interpret[WeekDay] = {
    case WeekDay(weekday, Time(hour, minute)) => interpretHAndM(hour, minute) + " * * " + weekday
  }

  /**
   * 转换小时与分钟
   *
   * @param hour   小时
   * @param minute 分钟
   * @return 部分时间表达式
   */
  private def interpretHAndM(hour: Int, minute: Int): String = {
    s"${if (minute == -1) "*" else minute} ${if (hour == -1) "*" else hour}"
  }
}

object DefaultInterpretProtocol extends DefaultInterpretProtocol
