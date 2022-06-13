package o.lartifa.jam.plugins.push.command

import o.lartifa.jam.plugins.push.source.SourceIdentity

/**
 * 提示信息
 *
 * Author: sinar
 * 2022/6/10 00:00
 */
object Prompts {
  /**
   * 没有找到，请创建
   *
   * @param sourceIdentity 订阅源标识
   * @return 提示信息
   */
  def NotFoundPleaseCreate(sourceIdentity: SourceIdentity): String =
    s"""订阅不存在：$sourceIdentity
       |-----------------------
       |您可以使用 .订阅 添加 ${sourceIdentity.sourceType} ${sourceIdentity.sourceIdentity}
       |来创建该订阅""".stripMargin

  /**
   * 请为指令提供更多信息
   *
   * @param command 指令
   * @param info    信息
   * @return 提示信息
   */
  def PleaseProvideMoreInfoForCommand(command: String, info: String): String =
    s"""请提供$info
       |-------------------
       |指令正确格式举例：
       |.订阅 $command B站动态 uid""".stripMargin

  /**
   * 订阅不存在
   */
  val SubscriptionNotExist: String =
    """订阅不存在🤔
      |------------------
      |您可以发送 .订阅 列表
      |查看当前聊天的订阅信息""".stripMargin
}
