package o.lartifa.jam.plugins.trpg.data

/**
 * TRPG 玩家状态
 *
 * Author: sinar
 * 2021/8/15 22:46
 */
case class TRPGStatus(snapshotId: Long, gameId: Long, attrOverrides: String, tags: String)

object TRPGStatus {
  case class StatusChange
  (
    statusId: Long,
    name: String,
    adjustExpr: String,
    originValue: Int,
    afterValue: Int
  )

  case class RollHistory
  (
    statusId: Long,
    result: String,
    point: Int,
    pass: Boolean
  )
}
