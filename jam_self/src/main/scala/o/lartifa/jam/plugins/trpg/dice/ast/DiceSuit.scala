package o.lartifa.jam.plugins.trpg.dice.ast

import scala.util.Random

/**
 * 骰子组
 *
 * Author: sinar
 * 2021/7/15 23:08
 */
case class DiceSuit(name: String, rand: Random) {
  /**
   * Roll 一个骰子
   *
   * @param size 骰子大小
   * @return 点数
   */
  def roll(size: Int): Int = rand.nextInt(size) + 1
}

object DiceSuit {
  def apply(name: String): DiceSuit = {
    val seed = (name.hashCode.toString + System.currentTimeMillis()).hashCode
    new DiceSuit(name, new Random(seed))
  }

  /**
   * TODO 骰子可定义
   */
  val suits: Map[String, DiceSuit] = Map(
    "精致的水晶骰" -> DiceSuit("水晶骰"),
    "可爱的猫爪骰" -> DiceSuit("猫爪骰"),
    "有魄力的骨骰" -> DiceSuit("骨骰"),
    "普通的塑料骰" -> DiceSuit("骰子"),
    "黏黏的触手骰" -> DiceSuit("触手骰"),
    "冰凉的金属骰" -> DiceSuit("金属骰")
  )

  /**
   * 获取骰子
   *
   * @param name 名称
   * @return 骰子组
   */
  def get(name: String): DiceSuit = suits.getOrElse(name, DiceSuit.suits("精致的水晶骰"))
}
