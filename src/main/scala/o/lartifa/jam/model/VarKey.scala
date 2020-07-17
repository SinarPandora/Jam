package o.lartifa.jam.model

import o.lartifa.jam.model.VarKey.Category

/**
 * 变量键
 *
 * Author: sinar
 * 2020/7/18 00:21
 */
case class VarKey(name: String, category: Category)

object VarKey {
  sealed abstract class Category
  case object DB extends Category
  case object Temp extends Category
}
