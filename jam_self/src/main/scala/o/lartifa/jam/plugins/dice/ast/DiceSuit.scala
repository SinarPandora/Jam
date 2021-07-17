package o.lartifa.jam.plugins.dice.ast

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
}
