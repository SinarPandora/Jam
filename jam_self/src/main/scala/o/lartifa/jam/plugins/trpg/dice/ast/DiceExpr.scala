package o.lartifa.jam.plugins.trpg.dice.ast

import o.lartifa.jam.common.exception.ExecutionException

import scala.util.matching.Regex

/**
 * 投掷表达式
 *
 * Author: sinar
 * 2021/7/17 18:45
 */
case class DiceExpr(evaluable: Evaluable) {
  /**
   * 求值
   *
   * @param dice  骰子组
   * @param retry 重试次数
   * @return 投掷结果
   */
  @throws[ExecutionException]("表达式无法计算时，返回该异常")
  def tryEval(dice: DiceSuit, retry: Int = 0): SingleDiceResult = {
    try {
      if (retry > 5) {
        throw ExecutionException("表达式失败（如出现一除以零）次数过多，请调整表达式内容或再试一次")
      }
      val result = evaluable.calc(dice)
      if (result.isWhole) {
        // 是整数
        SingleDiceResult(result.intValue, None)
      } else {
        // 四舍五入
        val whole = result.setScale(0, BigDecimal.RoundingMode.HALF_UP).intValue
        SingleDiceResult(whole, Some(s"投掷结果（$whole）已舍入，原值：$result"))
      }
    } catch {
      case _: NumberFormatException | _: ArithmeticException => tryEval(dice, retry + 1)
    }
  }

  /**
   * 转换为相对可读的字符串形式
   *
   * @return 字符串
   */
  def readable: String = evaluable.readableStr
}

object DiceExpr {
  val DICE_EXPR: Regex = """([0-9]+)d([0-9]+)""".r("n", "size")

  /**
   * 构建投掷表达式
   *
   * @param expr 表达是字符串
   * @return 表达式对象
   */
  def apply(expr: String): DiceExpr = {
    val matchers = DICE_EXPR.findAllMatchIn(expr).toSeq
    val rollExprs = matchers.map(matcher => Roll(matcher.group("n").toInt, matcher.group("size").toInt)).toList
    val rollStrList = matchers.map(_.source.toString)
    val template = rollStrList.foldLeft(expr)((x, y) => x.replace(y, "%s"))
    DiceExpr(Evaluable(template, rollExprs))
  }
}
