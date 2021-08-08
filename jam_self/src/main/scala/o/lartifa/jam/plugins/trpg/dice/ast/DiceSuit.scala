package o.lartifa.jam.plugins.trpg.dice.ast

import o.lartifa.jam.model.CommandExecuteContext

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
   * 尝试获取骰子组
   * 搜索顺序：
   *  个人偏爱
   *  群偏爱
   *  系统默认
   *
   * @param context 指令上下文
   * @return 骰子组
   */
  def getSuit(implicit context: CommandExecuteContext): DiceSuit = ???
}
