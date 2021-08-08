package o.lartifa.jam.plugins.trpg.rule

import o.lartifa.jam.plugins.trpg.dice.ast.DiceSuit
import o.lartifa.jam.plugins.trpg.rule.Check.CheckResult
import o.lartifa.jam.plugins.trpg.rule.TRPGRule.SuccRules
import o.lartifa.jam.plugins.trpg.rule.TRPGRule.SuccRules.SuccRule

/**
 * 校验
 *
 * Author: sinar
 * 2021/8/8 02:16
 */
abstract class Check(val name: String, val prompt: Option[CheckPrompt]) {
  /**
   * 是否通过校验
   *
   * @param value 计算值
   * @return 校验结果
   */
  def pass(value: Int): Option[CheckResult]
}

object Check {
  case class CheckResult(result: String, private val _prompt: Option[CheckPrompt]) {
    /**
     * 按照概率随机获取提示信息
     *
     * @param diceSuit 骰子组
     * @return 提示信息
     */
    def prompt(diceSuit: DiceSuit): Option[String] = _prompt.flatMap {
      case CheckPrompt(prob, msgs) =>
        if (diceSuit.roll(100) < (prob * 100).intValue()) {
          Some(msgs(diceSuit.roll(msgs.size) - 1))
        } else None
    }
  }

  class RangeCheck(name: String, prompt: Option[CheckPrompt], range: Range) extends Check(name, prompt) {
    /**
     * 是否通过校验
     *
     * @param value 计算值
     * @return 校验结果
     */
    override def pass(value: Int): Option[CheckResult] = {
      if (range.start <= value && value <= range.end) {
        Some(CheckResult(name, prompt))
      } else None
    }
  }

  class PercentCheckF(name: String, prompt: Option[CheckPrompt], percent: Double, rule: SuccRule) {
    /**
     * 转换为范围校验
     *
     * @param start 范围起始点（应用百分比前）
     * @return 范围校验对象
     */
    def toRange(start: Int): RangeCheck = {
      val startOrEnd = (BigDecimal(start) * percent).setScale(0, BigDecimal.RoundingMode.HALF_UP).intValue
      rule match {
        case SuccRules.Gt | SuccRules.Ge => new RangeCheck(name, prompt, startOrEnd to 100)
        case SuccRules.Lt | SuccRules.Le => new RangeCheck(name, prompt, 0 to startOrEnd)
      }
    }
  }
}
