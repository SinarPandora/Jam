package o.lartifa.jam.plugins.trpg.data

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
)

object TRPGGameData {
  case class TRPGGameInitData
  (
    name: String,
    ruleName: String,
    kpListStr: String
  )
}
