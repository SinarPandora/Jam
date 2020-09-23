package o.lartifa.jam.plugins.love_and_hate

/**
 * 爱恨初始化器
 *
 * Author: sinar
 * 2020/9/6 01:35
 */
class LoveAndHateInitializer(favorLevels: Map[Int, Int]) {

}

object LoveAndHateInitializer {
  def apply(): LoveAndHateInitializer = {

    new LoveAndHateInitializer(Map.empty)
  }
}
