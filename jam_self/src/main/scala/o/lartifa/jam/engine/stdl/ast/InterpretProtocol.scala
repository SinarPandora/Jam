package o.lartifa.jam.engine.stdl.ast

/**
 * SDExp 解析协议定义
 *
 * Author: sinar
 * 2021/1/1 14:25
 */
trait InterpretProtocol {
  implicit val interpretCronExp: Interpret[CronExpression]
  implicit val interpretTime: Interpret[Time]
  implicit val interpretDate: Interpret[Date]
  implicit val interpretEachAt: Interpret[WeekDay]
}
