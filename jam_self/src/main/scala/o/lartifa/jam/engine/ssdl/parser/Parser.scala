package o.lartifa.jam.engine.ssdl.parser

import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.model.behaviors.StringToVarKey

/**
 * 解析器工具方法
 *
 * Author: sinar
 * 2020/1/4 15:16
 */
trait Parser extends StringToVarKey {
  /**
   * 检查参数是否满足条件
   *
   * @param require 条件
   * @param elsePrint 错误提示信息
   */
  protected def need(require: Boolean, elsePrint: String): Unit = if (!require) throw ParseFailException(elsePrint)
}
