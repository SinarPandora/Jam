package o.lartifa.jam.plugins.trpg.rule

import o.lartifa.jam.plugins.trpg.rule.Check.CheckResult
import o.lartifa.jam.plugins.trpg.rule.TRPGRule.SuccRules

/**
 * 基准线
 *
 * Author: sinar
 * 2021/7/17 17:37
 */
case class Baseline(baseline: Int, rule: TRPGRule) {
  /**
   * 校验投掷结果和基准线
   *
   * 成功校验顺序：大成功 -> 极难成功 -> 困难成功 -> 普通校验
   * 失败校验顺序：大失败 -> 普通校验
   *
   * @param value 投掷结果
   * @return 校验结果
   */
  def check(value: Int): CheckResult = {
    // 此处肯定会有一个成功的结果
    val pass = rule.succRule match {
      case SuccRules.Gt => value > baseline
      case SuccRules.Ge => value >= baseline
      case SuccRules.Lt => value < baseline
      case SuccRules.Le => value <= baseline
    }
    val baseResult = CheckResult(if (pass) "成功" else "失败", None)
    (if (pass) {
      rule.succCheckers.flatMap {
        case Left(percentCheckF) => percentCheckF.toRange(baseline).pass(value)
        case Right(check) => check.pass(value)
      }
    } else {
      rule.failCheckers.flatMap {
        case Left(percentCheckF) => percentCheckF.toRange(baseline).pass(value)
        case Right(check) => check.pass(value)
      }
    }).headOption.getOrElse(baseResult)
  }
}
