package o.lartifa.jam.plugins.trpg.data

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.database.temporary.Memory.database.db
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.plugins.trpg.data.TRPGGameData.{TRPGGameInitData, serialize}
import o.lartifa.jam.plugins.trpg.data.TRPGStatus.{RollHistory, RollMetric, StatusChange}
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
object TRPGDataRepo {

  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(this.getClass)

  private implicit val threadPool: ExecutionContext = ThreadPools.DB

  import o.lartifa.jam.database.temporary.Memory.database.profile.api._

  /**
   * 创建游戏
   *
   * @param data 游戏数据
   * @return 游戏 id
   */
  def createGameData(data: TRPGGameInitData): Future[Long] = db.run {
    (TrpgGame.map(row => (row.name, row.ruleName, row.kpList)) returning TrpgGame.map(_.id)) += (
      (data.name, data.ruleName, data.kpListStr)
      )
  }

  /**
   * 加入游戏
   *
   * @param actorId 角色 id
   * @param gameId  游戏 id
   * @return 加入成功时返回游戏角色
   */
  def addPlayerToGame(actorId: Long, gameId: Long): Future[Either[String, TRPGPlayer]] = async {
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
            statusId <- (TrpgStatus.map(row => (row.snapshotId, row.gameId)) returning TrpgStatus.map(_.id)
              ) += ((snapshotId, gameId))
          } yield (snapshotId, statusId)).transactionally
          val (snapshotId, statusId) = await(db.run(transaction))
          Right(TRPGPlayer(actor, snapshotId, statusId, gameId))
        case None => Left("指定角色不存在")
      }
    }
  }

  /**
   * 根据名字查找游戏数据
   *
   * @param name 游戏名
   * @return 搜索结果
   */
  def findGameDataByName(name: String): Future[Seq[TrpgGameRow]] = db.run {
    TrpgGame.filter(_.name === name).result
  }

  /**
   * 保存游戏元数据
   *
   * @param data 游戏数据
   * @return 保存结果
   */
  def saveGameMetaData(data: TrpgGameRow): Future[Boolean] = db.run {
    TrpgGame.filter(_.id === data.id).update(data)
  }.map(_ == 1)


  /**
   * 保存游戏数据（基本数据 + 角色状态）
   *
   * @param data 游戏数据
   * @return 保存结果
   */
  def saveMetadataAndStatus(data: TRPGGameData): Future[Unit] = {
    val transaction = (
      for {
        _ <- TrpgGame.filter(_.id === data.id).update(serialize(data))
        _ <- DBIO.sequence(data.player.values.map(_.statusToSave).map(it => TrpgStatus.update(it)))
      } yield ()
      ).transactionally
    db.run(transaction)
  }

  /**
   * 更新快照属性
   * * 只有拥有者和 KP 可以修改
   *
   * @param snapshotId 快照 id
   * @param qId        QQ号
   * @param attrStr    属性字符串
   * @return 更新结果
   */
  def updateSnapshotAttr(snapshotId: Long, qId: Long, attrStr: String): Future[Either[String, Unit]] = db.run {
    TrpgActorSnapshot
      .join(TrpgStatus).on((snapshot, status) => snapshot.id === status.snapshotId)
      .join(TrpgGame).on { case ((_, status), game) => status.gameId === game.id }
      .filter {
        case ((snapshot, _), game) =>
          snapshot.id === snapshotId && (snapshot.qid === qId || (game.kpList like s"%$qId%"))
      }
      .map { case ((snapshot, _), _) => snapshot.attr }
      .update(attrStr)
  }.map(count => {
    if (count != 1) Left("请确认角色存在，且你是角色拥有者或者本场游戏的KP")
    else Right()
  })

  /**
   * 同步角色属性到快照
   *
   * @param snapshotId 快照 id
   * @return 更新结果
   */
  def syncActorAttrToSnapshot(snapshotId: Long): Future[Either[String, String]] = async {
    await(db.run(TrpgActorSnapshot.filter(_.id === snapshotId).result.headOption)) match {
      case Some(snapshot) =>
        val attrStr = await(db.run(TrpgActor.filter(_.id === snapshot.actorId).map(_.attr).result.head))
        await(db.run(TrpgActorSnapshot.update(snapshot.copy(attr = attrStr))))
        Right(attrStr)
      case None => Left("指定快照不存在")
    }
  }

  /**
   * 从游戏中移除指定角色（快照）
   * * 关联数据将全部移除
   * * 只有拥有者和 KP 可以移除
   *
   * @param gameId     游戏 id
   * @param qId        操作者 QQ 号
   * @param snapshotId 快照 id
   * @return
   */
  def dropSnapshotFromGame(gameId: Long, qId: Long, snapshotId: Long): Future[Either[String, Boolean]] = async {
    await(db.run(TrpgGame.filter(_.id === gameId).result.headOption)) match {
      case Some(game) =>
        await(db.run(TrpgActorSnapshot.filter(_.id === snapshotId).result.headOption)) match {
          case Some(snapshot) =>
            if (game.kpList.split(",").contains(qId.toString) || snapshot.qid == qId) {
              val result = await(db.run(TrpgActorSnapshot.filter(_.id === snapshotId).delete))
              Right(result == 1)
            } else Left("请确保你是角色的拥有者或者本场游戏的KP")
          case None => Left("指定角色并不存在于当前游戏中")
        }
      case None => Left("游戏不存在")
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
   * 分页列出状态变更记录
   *
   * @param statusId 状态 id
   * @param pageSize 每页大小
   * @param page     当前页数
   * @return 变更记录
   */
  def listStatusChanges(statusId: Long, pageSize: Int = 10, page: Int = 1): Future[Seq[TrpgStatusChangeHistoryRow]] = db.run {
    TrpgStatusChangeHistory
      .filter(_.statusId === statusId)
      .take(pageSize)
      .drop((page - 1) * pageSize)
      .sortBy(_.id.desc)
      .result
  }

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
   * 获取投掷统计
   *
   * @param statusId 状态 id
   * @return 投掷统计
   */
  def getRollMetricBySnapshotId(statusId: Long): Future[Option[RollMetric]] = db.run {
    sql"""select count(id)              as total,
         |       count(pass = true)     as totalPass,
         |       count(result = '大成功')  as totalHugeSuccess,
         |       count(result = '困难成功') as totalHardSuccess,
         |       count(result = '极难成功') as totalVeryHardSuccess,
         |       count(result = '大失败')  as totalHugeFail
         |from trpg_roll_history
         |where status_id = $statusId""".stripMargin.as[RollMetric].headOption
  }

  /**
   * 获取投掷统计
   *
   * @param qId QQ 号
   * @return 投掷统计
   */
  def getRollMetricByQId(qId: Long): Future[Option[RollMetric]] = db.run {
    sql"""select count(history.id)              as total,
         |       count(history.pass = true)     as totalPass,
         |       count(history.result = '大成功')  as totalHugeSuccess,
         |       count(history.result = '困难成功') as totalHardSuccess,
         |       count(history.result = '极难成功') as totalVeryHardSuccess,
         |       count(history.result = '大失败')  as totalHugeFail
         |from trpg_roll_history history
         |         inner join trpg_status status
         |                    on history.status_id = status.id
         |         inner join trpg_actor_snapshot snapshot
         |                    on snapshot.id = status.snapshot_id
         |where snapshot.qid = $qId""".stripMargin.as[RollMetric].headOption
  }

  /**
   * 通过 Id 加载游戏数据
   *
   * @param id        游戏 id
   * @param chatInfo  会话信息
   * @param senderQId 发送者 QQ
   * @return 游戏数据
   */
  def loadGameDataById(id: Long, chatInfo: ChatInfo, senderQId: Long): Future[Either[String, TRPGGameData]] = async {
    await(db.run(TrpgGame.filter(_.id === id).result.headOption)) match {
      case Some(TrpgGameRow(id, ruleName, kpList, name, _)) =>
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
  def saveStatusData(status: TrpgStatusRow): Future[Boolean] = db.run {
    TrpgStatus.update(status)
  }.map(_ == 1)

  /**
   * 查找指定好友的全部角色
   *
   * @param qId QQ 号
   * @return 角色列表
   */
  def listActorsByQId(qId: Long): Future[Seq[TrpgActorRow]] = db.run {
    TrpgActor.filter(_.qid === qId).result
  }

  /**
   * 启用/禁用角色
   *
   * @param actorId  角色 id
   * @param qId      操作者 QQ
   * @param isActive 角色状态
   * @return 操作结果
   */
  def setActorActiveStatus(actorId: Long, qId: Long, isActive: Boolean): Future[Either[String, Boolean]] = async {
    await(db.run(TrpgActor.filter(_.id === actorId).result.headOption)) match {
      case Some(actor) =>
        if (actor.qid != qId) Left("不可以操作不属于你的角色")
        else Right(await(db.run(TrpgActor.update(actor.copy(isActive = isActive)))) == 1)
      case None => Left("指定角色不存在")
    }
  }

  /**
   * 更新角色信息
   *
   * @param qId   操作者 QQ
   * @param actor 角色数据
   * @return 操作结果
   */
  def updateActor(qId: Long, actor: TrpgActorRow): Future[Either[String, Boolean]] = async {
    await(db.run(TrpgActor.filter(_.id === actor.id).result.headOption)) match {
      case Some(actor) =>
        if (actor.qid != qId) Left("不可以操作不属于你的角色")
        else Right(await(db.run(TrpgActor.update(actor))) == 1)
      case None => Left("指定角色不存在")
    }
  }

  /**
   * 通过 Id 查找角色
   *
   * @param actorId 角色 id
   * @return 角色数据
   */
  def getActorById(actorId: Long): Future[Option[TrpgActorRow]] = db.run {
    TrpgActor.filter(_.id === actorId).result.headOption
  }

  /**
   * 保存角色
   *
   * @param actor 角色数据
   * @return
   */
  def addActor(actor: TrpgActorRow): Future[Long] = db.run {
    (TrpgActor.map(row =>
      (row.qid, row.attr, row.info, row.name, row.defaultConfig, row.isActive)) returning TrpgActor.map(_.id)
      ) += ((actor.qid, actor.attr, actor.info, actor.name, actor.defaultConfig, true))
  }
}
