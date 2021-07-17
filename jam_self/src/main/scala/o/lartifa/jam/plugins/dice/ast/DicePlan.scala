package o.lartifa.jam.plugins.dice.ast

import o.lartifa.jam.common.exception.ExecutionException

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

/**
 * 投掷计划
 *
 * Author: sinar
 * 2021/7/17 19:00
 */
case class DicePlan(title: String, expr: DiceExpr, times: Int) {
  /**
   * 单次/多次连续投掷结果
   * （只有结果，不输出投掷类型和表达式）
   *
   * @param dice 骰子组
   * @return 投掷结果
   */
  @throws[ExecutionException]("表达式无法计算时，返回该异常")
  def resultAsList(dice: DiceSuit): Seq[DiceResult] = (1 to times).map(_ => expr.tryEval(dice))

  /**
   * 带有基准线的单次/多次连续投掷结果
   * （只有结果，不输出投掷类型和表达式）
   *
   * @param dice     骰子组
   * @param baseline 基准线（传入 None 时不进行基准线判断）
   * @return 投掷通过结果
   */
  @throws[ExecutionException]("表达式无法计算时，返回该异常")
  def result(dice: DiceSuit, baseline: Option[Baseline] = None): MultiDiceResult = {
    val results = ListBuffer[String]()
    val extraInfo = ListBuffer[String]()
    val sum = resultAsList(dice).zipWithIndex.tapEach {
      case (DiceResult(value, extra), i) =>
        val idx = i + 1
        extra.foreach(it => extraInfo += s"$idx：$it")
        results += s"$idx：$value${baseline.flatMap(_.check(value).map(it => s"【$it】")).getOrElse("")}"
    }.map(_._1.value).sum
    MultiDiceResult(sum, results.toList, extraInfo.toList)
  }
}

object DicePlan {
  private val EXPR_VALIDATE: Regex = """([0-9]#)?([0-9*/+\-()d%=?:]+)""".r("times", "expr")

  /**
   * 解析表达式并构建投掷计划
   *
   * @param string 字符串
   * @return 构建结果
   */
  def apply(string: String): Either[String, DicePlan] = {
    val noSpace = string.replaceAll("\\W", "").toLowerCase
    EXPR_VALIDATE.findFirstMatchIn(noSpace) match {
      case Some(matcher) =>
        val times = if (matcher.group("times") != null) matcher.group("times").toInt else 1
        val expr = DiceExpr(matcher.group("expr"))
        val title = (if (times != 1) times + "次：" else "") + expr.readable
        Right(DicePlan(title, expr, times))
      case None => Left("未发现有效的投掷表达式")
    }
  }
}
