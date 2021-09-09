package o.lartifa.jam.plugins.trpg.data

import slick.jdbc.GetResult

/**
 * TRPG 状态
 *
 * Author: sinar
 * 2021/8/22 22:45
 */
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

  case class RollMetric
  (
    total: Int,
    totalPass: Int,
    totalHardSuccess: Int,
    totalVeryHardSuccess: Int,
    totalHugeSuccess: Int,
    totalHugeFail: Int
  )

  implicit val getRollMetric: GetResult[RollMetric] = GetResult(r =>
    RollMetric(
      total = r.nextInt(),
      totalPass = r.nextInt(),
      totalHardSuccess = r.nextInt(),
      totalVeryHardSuccess = r.nextInt(),
      totalHugeSuccess = r.nextInt(),
      totalHugeFail = r.nextInt()
    )
  )
}
