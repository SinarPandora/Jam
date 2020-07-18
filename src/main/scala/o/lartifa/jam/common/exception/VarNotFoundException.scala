package o.lartifa.jam.common.exception

import o.lartifa.jam.model.VarKey

/**
 * 变量缺失异常
 *
 * Author: sinar
 * 2020/1/4 16:25
 */
case class VarNotFoundException(paramName: String, category: String) extends Exception(s"没有找到名为：$paramName 的$category")

object VarNotFoundException {
  def apply(varKey: VarKey): VarNotFoundException = new VarNotFoundException(varKey.name, varKey.category.name)

  def apply(paramName: String, category: String = "变量"): VarNotFoundException = new VarNotFoundException(paramName, category)
}
