package o.lartifa.jam.engine.parser

import o.lartifa.jam.common.exception.ParseFailException

/**
 * 解析器工具方法
 *
 * Author: sinar
 * 2020/1/4 15:16 
 */
trait Parser {
  /**
   * 检查参数是否满足条件
   *
   * @param require 条件
   * @param message 错误提示信息
   */
  protected def need(require: Boolean, message: String): Unit = if (!require) throw ParseFailException(message)
}
