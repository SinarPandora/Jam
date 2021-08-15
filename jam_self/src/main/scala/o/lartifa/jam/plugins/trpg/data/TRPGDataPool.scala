package o.lartifa.jam.plugins.trpg.data

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.util.TimeUtil
import o.lartifa.jam.database.temporary.Memory.database.db
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.plugins.trpg.data.TRPGGameData.TRPGGameInitData
import o.lartifa.jam.plugins.trpg.data.TRPGStatus.{RollHistory, StatusChange}
import o.lartifa.jam.plugins.trpg.rule.RuleRepo
import o.lartifa.jam.pool.{JamContext, ThreadPools}

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * TRPG 数据池
 *
 * Author: sinar
 * 2021/8/14 18:44
 */
object TRPGDataPool {

  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(this.getClass)

  private implicit val threadPool: ExecutionContext = ThreadPools.DB

  import o.lartifa.jam.database.temporary.Memory.database.profile.api._

  /**
   * 创建游戏
   *
   * @param data 游戏数据
   * @return 游戏 Id
   */
  def createGame(data: TRPGGameInitData): Future[Long] = db.run {
    (TrpgGame.map(row => (row.name, row.ruleName, row.kpList)) returning TrpgGame.map(_.id)) += (
      (data.name, data.ruleName, data.kpListStr)
      )
  }

  /**
   * 加入游戏
   *
   * @param actorId 角色 Id
   * @param gameId  游戏 Id
   * @return 加入成功时返回游戏角色
   */
  def joinToGame(actorId: Long, gameId: Long): Future[Either[String, TRPGPlayer]] = async {
    val alreadyExist = await {
      db.run((
        TrpgStatus.filter(_.gameId === gameId)
          .join(TrpgActorSnapshot).on(_.snapshotId === _.id)
          .filter(_._2.actorId === actorId)
          .length =!= 0
        ).result)
    }
    if (alreadyExist) Left("本局游戏中已存在相同角色")
    else {
      val actorOpt = await(db.run(TrpgActor.filter(_.id === actorId).result.headOption))
      actorOpt match {
        case Some(actor) =>
          val transaction = (for {
            snapshotId <- (TrpgActorSnapshot.map(row => (row.actorId, row.name, row.qid, row.attr, row.info))
              returning TrpgActorSnapshot.map(_.id)
              ) += ((actor.id, actor.name, actor.qid, actor.attr, actor.info))
            _ <- TrpgStatus.map(row => (row.snapshotId, row.gameId)) += ((snapshotId, gameId))
          } yield snapshotId).transactionally
          val snapshotId = await(db.run(transaction))
          Right(TRPGPlayer(actor, snapshotId, gameId))
        case None => Left("指定角色不存在")
      }
    }
  }

  /**
   * 记录状态变更历史
   *
   * @param change 状态变更
   * @return 操作结果
   */
  def recordStatusChanges(change: StatusChange): Future[Boolean] = db.run {
    TrpgStatusChangeHistory
      .map(row => (row.statusId, row.name, row.adjustExpr, row.originValue, row.afterValue)) += (
      (change.statusId, change.name, change.adjustExpr, change.originValue, change.afterValue)
      )
  }.map(_ == 1)

  /**
   * 记录投掷历史
   *
   * @param rollHistory 投掷历史
   * @return 操作结果
   */
  def recordRollHistory(rollHistory: RollHistory): Future[Boolean] = db.run {
    TrpgRollHistory.map(row => (row.statusId, row.result, row.point, row.pass)) += (
      (rollHistory.statusId, rollHistory.result, rollHistory.point, rollHistory.pass)
      )
  }.map(_ == 1)

  /**
   * 通过 Id 加载游戏数据
   *
   * @param id        游戏 Id
   * @param chatInfo  会话信息
   * @param senderQId 发送者 QQ
   * @return 游戏数据
   */
  def loadGameById(id: Long, chatInfo: ChatInfo, senderQId: Long): Future[Either[String, TRPGGameData]] = async {
    await(db.run(TrpgGame.filter(_.id === id).result.headOption)) match {
      case Some(TrpgGameRow(id, ruleName, kpList, name)) =>
        Try(kpList.split(",").map(_.toLong).toList) match {
          case Failure(exception) =>
            logger.error(exception)
            Left("KP列表获取错误")
          case Success(kpIdList) =>
            if (!kpIdList.contains(senderQId)) Left("您不是该游戏的KP，无法开始游戏")
            else RuleRepo.rules.get(ruleName) match {
              case Some(rule) =>
                val playerStatus = await(db.run(TrpgStatus.filter(_.gameId === id).result))
                val snapshotId = playerStatus.map(_.snapshotId)
                val actors = await(db.run(TrpgActorSnapshot.filter(_.id inSet snapshotId).result))
                if (snapshotId.sizeIs != actors.size) {
                  Left("部分角色无法找到，游戏无法继续开启")
                } else {
                  val actorIdMap = actors.map(it => it.id -> it).toMap
                  val players = playerStatus.map(it => TRPGPlayer(actorIdMap(it.snapshotId), it))
                    .map(it => it.qid -> it)
                    .toMap
                  Right {
                    TRPGGameData(id = id, kpList = kpIdList, rule = rule, name = name, player = players, chatInfo = chatInfo)
                  }
                }
              case None => Left(s"初始化失败，没找到对应的规则，规则名：$ruleName")
            }
        }
      case None => Left("未找到对应的游戏")
    }
  }

  /**
   * 保存状态
   *
   * @param status 状态对象
   * @return 是否保存成功
   */
  def saveStatus(status: TRPGStatus): Future[Boolean] = {
    db.run {
      TrpgStatus.filter(row => row.gameId === status.gameId && row.snapshotId === status.snapshotId)
        .map(row => (
          row.attrOverrides, row.tags, row.updateTime
        )).update((status.attrOverrides, status.tags, TimeUtil.currentTimeStamp))
    }.map(_ == 1)
  }
}
