package o.lartifa.jam.model.behaviors

import o.lartifa.jam.model.VarKey
import o.lartifa.jam.model.VarKey.{DB, Temp}

/**
 * 行为：字符串等于变量键
 *
 * Author: sinar
 * 2020/7/18 00:37
 */
trait StringAsVarKey {
  /**
   * 在默认情况下，字符串作为数据库变量键
   *
   * @param strKey 字符串
   * @return 变量键
   */
  protected implicit def StringAsDBVarKeyByDefault(strKey: String): VarKey = VarKey(strKey, DB)

  /**
   * 在默认情况下，变量键的名字可以直接作为变量名
   *
   * @param varKey 变量键
   * @return 变量键
   */
  protected implicit def VarKeyNameIsString(varKey: VarKey): String = varKey.name

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
