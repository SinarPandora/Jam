package o.lartifa.jam.plugins.trpg.data

import o.lartifa.jam.common.util.TimeUtil
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.plugins.trpg.data.TRPGPlayer.{PlayerConfig, Tags}
import o.lartifa.jam.plugins.trpg.rule.Attr
import o.lartifa.jam.plugins.trpg.rule.Attr.Attrs

/**
 * TRPG 玩家
 *
 * Player = Actor + Status
 *
 * Author: sinar
 * 2021/7/24 21:42
 */
case class TRPGPlayer
(
  gameId: Long,
  statusId: Long,
  snapshotId: Long,
  qid: Long,
  name: String,
  info: String,
  private val _attrs: Attrs,
  attrOverrides: Attrs,
  config: PlayerConfig,
  tags: Tags
) {
  /**
   * 获取经过调整的全部属性
   *
   * @return 全部属性列表
   */
  def attrs: Map[String, Attr] = _attrs ++ attrOverrides

  /**
   * 转换为状态字符串，用于保存状态
   *
   * @return 状态字符串
   */
  def statusToSave: TrpgStatusRow = TrpgStatusRow(
    id = statusId,
    snapshotId = snapshotId,
    gameId = gameId,
    attrOverrides = ujson.write(Attr.attrsToJson(attrOverrides)),
    tags = ujson.write(tags),
    config = ujson.write(config),
    updateTime = TimeUtil.currentTimeStamp
  )
}

object TRPGPlayer {

  type Tags = Map[String, String]
  type PlayerConfig = Map[String, String]

  /**
   * 初始化 Player 数据
   * * 仅在玩家初次加入游戏时使用
   *
   * @param actor      角色基本数据
   * @param snapshotId 快照 Id
   * @param statusId   状态 Id
   * @param gameId     游戏 Id
   * @return 初始化的 Player 数据
   */
  def apply(actor: TrpgActorRow, snapshotId: Long, statusId: Long, gameId: Long): TRPGPlayer = {
    // 载入默认设置
    val config = ujson.read(actor.defaultConfig).obj.map {
      case (key, value) => key -> value.str
    }.toMap
    new TRPGPlayer(
      gameId = gameId,
      statusId = statusId,
      snapshotId = snapshotId,
      qid = actor.qid,
      name = actor.name,
      info = actor.info,
      _attrs = Attr.empty,
      attrOverrides = Attr.empty,
      config = config,
      tags = Map.empty
    )
  }

  /**
   * 创建 Player 数据
   *
   * Player = Actor + Status
   *
   * @param snapshot 角色快照
   * @param status   状态
   * @return 角色 + 状态 = 玩家
   */
  def apply(snapshot: TrpgActorSnapshotRow, status: TrpgStatusRow): TRPGPlayer = {
    val attrs: Attrs = Attr.jsonToAttrs(ujson.read(snapshot.attr))
    val attrOverrides: Attrs = Attr.jsonToAttrs(ujson.read(status.attrOverrides))
    val tags = ujson.read(status.tags).obj.map {
      case (key, value) => key -> value.str
    }.toMap
    val config = ujson.read(status.config).obj.map {
      case (key, value) => key -> value.str
    }.toMap

    // 构建
    new TRPGPlayer(
      gameId = status.gameId,
      statusId = status.id,
      snapshotId = snapshot.id,
      qid = snapshot.qid,
      name = snapshot.name,
      info = snapshot.info,
      _attrs = attrs,
      attrOverrides = attrOverrides,
      config = config,
      tags = tags
    )
  }
}
