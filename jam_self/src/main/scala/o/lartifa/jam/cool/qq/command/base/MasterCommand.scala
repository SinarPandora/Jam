package o.lartifa.jam.cool.qq.command.base

import cc.moecraft.icq.command.CommandProperties

/**
 * 监管者指令 辅助工具
 *
 * Author: sinar
 * 2020/1/5 12:04 
 */
trait MasterCommand {

  /**
   * 不回复内容
   */
  val NO_RESPONSE: String = ""

  /**
   * 创建指令捕获前缀属性
   *
   * @param prefix 前缀列表
   * @return 捕获属性
   */
  def createProperties(prefix: Seq[String]): CommandProperties = if (prefix.lengthIs > 1) {
    new CommandProperties(prefix.head, prefix.tail: _*)
  } else new CommandProperties(prefix.head)

}
