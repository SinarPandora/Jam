package o.lartifa.jam.plugins.dice.ast

import o.lartifa.jam.plugins.dice.ast.Baseline.Checker

/**
 * 基准线
 *
 * Author: sinar
 * 2021/7/17 17:37
 */
case class Baseline(checkers: Seq[Checker]) {
  /**
   * 校验投掷结果和基准线
   *
   * @return 校验结果
   */
  def check(result: Int): Option[String] = {
    checkers.flatMap(_.apply(result)).headOption
  }
}

object Baseline {
  /**
   * 基准校验器
   */
  type Checker = Int => Option[String]
}
