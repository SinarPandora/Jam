package o.lartifa.jam.plugins.story_runner

import o.lartifa.jam.common.util.TimeUtil
import o.lartifa.jam.database.temporary.schema.Tables
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.model.ChatInfo

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

/**
 * 故事数据存储默认实现
 *
 * Author: sinar
 * 2021/2/6 16:33
 */
object StoryRepo {

  case class NameAndAuthor(name: String, author: String)

  import o.lartifa.jam.database.temporary.Memory.database.db
  import o.lartifa.jam.database.temporary.Memory.database.profile.api._

  private implicit val repoScopedDataTransformEC: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newWorkStealingPool(Runtime.getRuntime.availableProcessors() * 2))

  /**
   * 列出全部故事
   *
   * @return 故事列表
   */
  def stories(): Future[Seq[StorySimpleInfo]] = db.run {
    Story.filter(_.status === StoryStatus.IN_USE)
      .map(r => (r.id, r.path, r.name, r.keyword, r.author)).result
  }.map(_.map {
    case (id, path, name, keyword, author) => StorySimpleInfo(id, path, name, keyword, author)
  })

  /**
   * 根据 ID 获取故事
   *
   * @param storyId 故事 ID
   * @return 故事
   */
  def story(storyId: Long): Future[Option[Tables.StoryRow]] = db.run {
    Story.filter(r => r.status === StoryStatus.IN_USE && r.id === storyId).result.headOption
  }

  /**
   * 添加故事
   *
   * @param initialInfo 初始化数据
   * @return 添加结果
   */
  def addStory(initialInfo: StoryInfo): Future[Boolean] = db.run {
    initialInfo match {
      case StoryInfo(path, name, checksum, keyword, author, script, status, defaultConfig) =>
        Story.map(r => (r.path, r.name, r.checksum, r.keyword, r.author, r.script, r.status, r.defaultConfig)) += ((
          path, name, checksum, keyword, author, script, status, defaultConfig.jsonStr
        ))
    }
  }.map(_ == 1)

  /**
   * 标记故事状态为遗留
   *
   * @param nameAndAuthor 名字与作者（查找用）
   * @return 遗留故事 ID
   */
  def markStoryAsLegacy(nameAndAuthor: NameAndAuthor): Future[Option[Long]] = {
    nameAndAuthor match {
      case NameAndAuthor(name, author) =>
        db.run(Story.filter(it => it.name === name && it.author === author).map(_.id).result.headOption)
          .flatMap {
            case Some(id) =>
              db.run {
                Story.filter(_.id === id).map(_.status).update(StoryStatus.LEGACY)
              }.map(_ => Some(id))
            case None => Future.successful(None)
          }
    }
  }

  /**
   * 临时禁用某故事
   *
   * @param storyId 故事的唯一 ID
   * @return 更新结果
   */
  def markStoryAsInactive(storyId: Long): Future[Boolean] = db.run {
    Story.filter(r => r.id === storyId && r.status === StoryStatus.IN_USE)
      .map(_.status).update(StoryStatus.INACTIVE)
  }.map(_ == 1)

  /**
   * 重新启用某故事
   *
   * @param storyId 故事的唯一 ID
   * @return 更新结果
   */
  def markStoryAsActive(storyId: Long): Future[Boolean] = db.run {
    Story.filter(r => r.id === storyId && r.status === StoryStatus.INACTIVE)
      .map(_.status).update(StoryStatus.IN_USE)
  }.map(_ == 1)

  /**
   * 查找存档继承关系
   *
   * @param storyId 故事 ID
   * @return 查询结果
   */
  def inheritRelation(storyId: Long): Future[Option[Tables.StorySaveInheritRow]] = db.run {
    StorySaveInherit.filter(_.legacyStoryId === storyId).result.headOption
  }

  /**
   * 创建存档继承关系
   *
   * @param legacyStoryId 遗留故事 ID
   * @param newStoryId    新故事 ID
   * @return 创建结果
   */
  def createInheritRelation(legacyStoryId: Long, newStoryId: Long): Future[Boolean] = db.run {
    StorySaveInherit.map(r => (r.legacyStoryId, r.inUseStoryId)) += ((legacyStoryId, newStoryId))
  }.map(_ == 1)

  /**
   * 迁移存档
   *
   * @param saveId        存档 ID
   * @param targetStoryId 迁移目标 ID
   * @return 迁移结果
   */
  def saveFileMigration(saveId: Long, targetStoryId: Long): Future[Boolean] = db.run {
    StorySaveFile.filter(_.id === saveId).map(_.storyId).update(targetStoryId)
  }.map(_ == 1)

  /**
   * 迁移全部存档
   *
   * @param legacyStoryId 遗留故事 ID
   * @param targetStoryId 迁移目标 ID
   * @return 迁移结果
   */
  def migrateAllSaveFile(legacyStoryId: Long, targetStoryId: Long): Future[Int] = db.run {
    StorySaveFile.filter(_.storyId === legacyStoryId).map(_.storyId).update(targetStoryId)
  }

  /**
   * 存档
   *
   * @param storyId  故事 ID
   * @param saveFile 存档
   * @return 保存结果
   */
  def save(storyId: Long, saveFile: StorySaveFileRow): Future[Boolean] = db.run {
    StorySaveFile += saveFile
  }.map(_ == 1)

  /**
   * 获取全部存档
   *
   * @param storyId  故事 ID
   * @param chatInfo 会话属性
   * @return 全部存档信息
   */
  def saveList(storyId: Long, chatInfo: ChatInfo): Future[Seq[StorySaveFileRow]] = {
    chatInfo match {
      case ChatInfo(chatType, chatId) => db.run {
        StorySaveFile.filter(it => it.storyId === storyId && it.chatId === chatId && it.chatType === chatType)
          .result
      }
    }
  }

  /**
   * 删除存档
   *
   * @param saveFileId 存档 ID
   * @return 删除结果
   */
  def deleteSave(saveFileId: Long): Future[Boolean] = db.run {
    StorySaveFile.filter(it => it.id === saveFileId).delete
  }.map(_ == 1)

  /**
   * 加载最新的存档为状态对象
   *
   * @param storyId  故事 ID
   * @param chatInfo 会话信息
   * @return 最新的状态对象
   */
  def loadLatestSaveFile(storyId: Long, chatInfo: ChatInfo): Future[Option[RunnerState]] = {
    chatInfo match {
      case ChatInfo(chatType, chatId) => db.run {
        StorySaveFile.filter(it => it.storyId === storyId && it.chatId === chatId && it.chatType === chatType)
          .sortBy(_.recordTime.desc)
          .result
          .headOption
      }.map(_.map(RunnerState.load))
    }
  }

  /**
   * 创建或更新配置
   *
   * @param storyId  故事 ID
   * @param chatInfo 会话信息
   * @param config   故事配置
   * @return 是否更新成功
   */
  def createOrUpdateConfig(storyId: Long, chatInfo: ChatInfo, config: StoryConfig): Future[Boolean] = {
    chatInfo match {
      case ChatInfo(chatType, chatId) => db.run {
        StoryRunnerConfig.filter(it => it.storyId === storyId && it.chatId === chatId && it.chatType === chatType)
          .result.headOption
      }.flatMap {
        case Some(_) => db.run {
          StoryRunnerConfig.filter(it => it.storyId === storyId && it.chatId === chatId && it.chatType === chatType)
            .map(row => (row.config, row.lastUpdate)).update((config.jsonStr, TimeUtil.currentTimeStamp))
        }
        case None => db.run {
          StoryRunnerConfig += StoryRunnerConfigRow(
            -1, storyId, chatType, chatId, config.jsonStr, TimeUtil.currentTimeStamp
          )
        }
      }.map(_ == 1)
    }
  }

  /**
   * 迁移配置文件
   *
   * @param fromId 来源故事 ID
   * @param toId   目标故事 ID
   */
  def migrateConfig(fromId: Long, toId: Long): Future[Unit] = db.run {
    StoryRunnerConfig.filter(it => it.storyId === fromId)
      .map(_.storyId)
      .update(toId)
  }.flatMap(_ => Future.unit)

  /**
   * 通过故事 ID 删除全部相关配置
   *
   * @param storyId 故事 ID
   */
  def deleteAllConfig(storyId: Long): Future[Unit] = db.run {
    StoryRunnerConfig.filter(it => it.storyId === storyId).delete
  }.flatMap(_ => Future.unit)

  /**
   * 清理升级故事产生的遗留关系数据，此方法应该也可以用于每夜维护任务
   * TODO 移动到 task
   * <p>
   * 第一步：如果故事支持无痛迁移，迁移全部存档
   * <br />
   * 第二步：如果这是一个遗留故事，并且没有任何对应的存档存在，则清理继承关系和故事数据
   * </p>
   *
   * @return 清理结果
   */
  def storyCleanup(): Future[Unit] = {
    // 如果故事支持无痛迁移，在睡觉时迁移全部存档

    // 如果这是一个遗留故事，并且没有任何对应的实例和存档存在，则清理继承关系和故事数据
    val query = sql"""select distinct s.id
            from public.story s
                left join public.story_save_file ssf on s.id = ssf.story_id
            where status = '遗留'
              and ssf.id is null""".as[Long]
    db.run(query).flatMap(needCleanup => db.run {
      DBIO.seq(
        StorySaveInherit.filter(_.legacyStoryId.inSet(needCleanup)).delete,
        Story.filter(_.id.inSet(needCleanup)).delete
      )
    })
  }
}
