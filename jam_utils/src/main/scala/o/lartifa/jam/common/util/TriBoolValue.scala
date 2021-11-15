package o.lartifa.jam.common.util

/**
 * 三向布尔
 *
 * Author: sinar
 * 2021/6/13 11:34
 */
object TriBoolValue extends Enumeration {
  type TriBool = Value
  /**
   * 布尔真
   */
  val True: TriBool = Value("true")
  /**
   * 布尔假
   */
  val False: TriBool = Value("false")
  /**
   * 全都要
   */
  val Both: TriBool = Value("both")
  /**
   * 未设置（null）
   */
  val Unset: TriBool = Value("unset")
}
