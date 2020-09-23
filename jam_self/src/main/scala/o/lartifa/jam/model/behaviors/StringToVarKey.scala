package o.lartifa.jam.model.behaviors

import o.lartifa.jam.model.VarKey
import o.lartifa.jam.model.VarKey.{DB, Temp}

/**
 * 行为：字符串转换为变量键
 *
 * Author: sinar
 * 2020/7/18 00:37
 */
trait StringToVarKey {

  /**
   * 转换字符串与变量键的隐式类
   *
   * @param strKey 字符串
   */
  implicit class StringVarKeyConverter(strKey: String) {
    def asDBVar: VarKey = VarKey(strKey, DB)

    def asTempVar: VarKey = VarKey(strKey, Temp)
  }

}
