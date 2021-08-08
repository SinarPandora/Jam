package o.lartifa.jam.plugins.trpg.dice.ast

import groovy.util.Eval
import o.lartifa.jam.plugins.trpg.rule.Check.CheckResult

/**
 * 骰子语法
 *
 * Author: sinar
 * 2021/7/15 23:19
 */
sealed trait AST {
  /**
   * 计算
   *
   * @param dice 骰子组
   * @return 结果
   */
  def calc(dice: DiceSuit): BigDecimal

  /**
   * 转换为可读的字符串形式
   *
   * @return 字符串
   */
  def readableStr: String
}

/**
 * 投掷
 *
 * @param n    N 个
 * @param size Dn
 */
case class Roll(n: Int, size: Int) extends AST {
  /**
   * 计算
   *
   * @param dice 骰子组
   * @return 结果
   */
  override def calc(dice: DiceSuit): BigDecimal = 1.0 * n * dice.roll(size)

  /**
   * 转换为可读的字符串形式
   *
   * @return 字符串
   */
  override def readableStr: String = s"${n}个D$size"
}

/**
 * 可求值对象
 *
 * @param template 非投表达式模板
 * @param rollActs 投掷动作
 */
case class Evaluable(template: String, rollActs: List[Roll]) extends AST {
  /**
   * 计算
   *
   * @param dice 骰子组
   * @return 结果
   */
  override def calc(dice: DiceSuit): BigDecimal = BigDecimal {
    Eval.me(template.format(rollActs.map(_.calc(dice)): _*)).toString
  }

  /**
   * 转换为可读的字符串形式
   * （不过对于复杂表达式来说真的可读吗）
   *
   * @return 字符串
   */
  override def readableStr: String = template.format(rollActs.map(it => s" ${it.readableStr} "))
}

/**
 * 投掷结果
 *
 * @param value     结果
 * @param extraInfo 附加信息
 */
case class SingleDiceResult(value: Int, extraInfo: Option[String])

/**
 * 多次投掷结果
 *
 * @param sum       合计结果
 * @param results   每次结果
 * @param extraInfo 附加信息
 */
case class DiceResult(sum: Int, results: List[String], extraInfo: List[String])

/**
 * 带有基准线的多次投掷结果
 *
 * @param sum       合计结果
 * @param results   每次结果
 * @param extraInfo 附加信息
 */
case class BaselineResult(sum: Int, results: List[CheckResult], extraInfo: List[String])
