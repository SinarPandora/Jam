package o.lartifa.jam.plugins

import o.lartifa.jam.model.VarKey

/**
 * Author: sinar
 * 2020/7/12 15:03
 */
package object picbot {

  object PatternMode {
    val ONLY: String = "仅当前"
    val RANGE: String = "范围内"
  }

  val CONFIG_ID: VarKey = VarKey("picbot_now_id", VarKey.DB)
  val CONFIG_ALLOWED_R18: VarKey = VarKey("picbot_allowed_r18", VarKey.DB)
  val CONFIG_MODE: VarKey = VarKey("picbot_mode", VarKey.DB)

  sealed abstract class Mode(val str: String)
  case object ONLY extends Mode("only")
  case object RANGE extends Mode("range")
}
