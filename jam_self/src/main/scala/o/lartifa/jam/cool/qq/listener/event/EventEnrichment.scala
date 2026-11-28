package o.lartifa.jam.cool.qq.listener.event

import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.pool.JamContext

import scala.util.Try

/**
 * 领域事件补全展示字段（群名 / 昵称）；失败时回落 QQ 号或配置名。
 *
 * Author: sinar
 * 2026/11/28
 */
object EventEnrichment {
  def botName: String = JamConfig.config.name

  def strangerNickname(qq: Long): String =
    Try(JamContext.apiClient.getStrangerInfo(qq).map(_.nickname).get)
      .toOption
      .filter(n => n != null && n.nonEmpty)
      .getOrElse(qq.toString)

  def groupName(groupId: Long): String =
    Try(JamContext.apiClient.getGroupInfo(groupId).map(_.groupName).get)
      .toOption
      .filter(n => n != null && n.nonEmpty)
      .getOrElse(groupId.toString)

  def groupCardOrNick(groupId: Long, qq: Long): String =
    Try {
      val info = JamContext.apiClient.getGroupMemberInfo(groupId, qq).get
      if (info.card.nonEmpty) info.card else info.nickname
    }.toOption
      .filter(n => n != null && n.nonEmpty)
      .getOrElse(qq.toString)
}
