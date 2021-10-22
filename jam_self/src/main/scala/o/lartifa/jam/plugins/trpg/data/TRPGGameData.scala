package o.lartifa.jam.plugins.trpg.data

import o.lartifa.jam.database.temporary.schema.Tables.*
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.plugins.trpg.rule.TRPGRule

/**
 * TRPG 游戏
 *
 * Author: sinar
 * 2021/7/24 21:26
 */
case class TRPGGameData
(
  id: Long,
  kpList: Seq[Long],
  rule: TRPGRule,
  name: String,
  player: Map[Long, TRPGPlayer],
  chatInfo: ChatInfo
) {
  override def toString: String = {
    s"""$name
       |游戏ID：$id
       |KP：${kpList.mkString("，")}
       |规则：${rule.name}""".stripMargin
  }
}

object TRPGGameData {
  case class TRPGGameInitData
  (
    name: String,
    ruleName: String,
    kpListStr: String
  )

  /**
   * 转换为序列化数据
   *
   * @param data 游戏数据
   * @return 数据库序列化数据
   */
  def serialize(data: TRPGGameData): TrpgGameRow =
    TrpgGameRow(
      id = data.id,
      name = data.name,
      ruleName = data.rule.name,
      kpList = data.kpList.mkString(","),
      lastChat = Some(data.chatInfo.serialize)
    )
}
