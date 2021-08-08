package o.lartifa.jam.plugins.trpg.dice.ast

import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.plugins.trpg.rule.Baseline
import o.lartifa.jam.plugins.trpg.rule.Check.CheckResult

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
  def resultAsList(dice: DiceSuit): Seq[SingleDiceResult] = (1 to times).map(_ => expr.tryEval(dice))

  /**
   * 带有基准线的单次/多次连续投掷结果
   * （只有结果，不输出投掷类型和表达式）
   *
   * @param dice 骰子组
   * @return 投掷通过结果
   */
  @throws[ExecutionException]("表达式无法计算时，返回该异常")
  def result(dice: DiceSuit): DiceResult = {
    val results = ListBuffer[String]()
    val extraInfo = ListBuffer[String]()
    val sum = resultAsList(dice).zipWithIndex.tapEach {
      case (SingleDiceResult(value, extra), i) =>
        val idx = i + 1
        extra.foreach(it => extraInfo += s"$idx：$it")
        results += s"$idx：$value"
    }.map(_._1.value).sum
    DiceResult(sum, results.toList, extraInfo.toList)
  }

  /**
   * 带有基准线的单次/多次连续投掷结果
   * （只有结果，不输出投掷类型和表达式）
   *
   * @param dice     骰子组
   * @param baseline 基准线
   * @return 投掷通过结果
   */
  @throws[ExecutionException]("表达式无法计算时，返回该异常")
  def result(dice: DiceSuit, baseline: Baseline): BaselineResult = {
    val results = ListBuffer[CheckResult]()
    val extraInfo = ListBuffer[String]()
    val sum = resultAsList(dice).zipWithIndex.tapEach {
      case (SingleDiceResult(value, extra), i) =>
        val idx = i + 1
        extra.foreach(it => extraInfo += s"$idx：$it")
        val resp = baseline.check(value)
        results += resp.copy(result = s"$idx：$value【${resp.result}】")
    }.map(_._1.value).sum
    BaselineResult(sum, results.toList, extraInfo.toList)
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
        val title = (if (times != 1) s"${times}次：" else "") + expr.readable
        Right(DicePlan(title, expr, times))
      case None => Left("未发现有效的投掷表达式")
    }
  }
}
