package o.lartifa.jam.plugins.story_runner

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
   * 创建对应的 instance
   *
   * @param storyId  故事 ID
   * @param chatInfo 会话属性
   * @return 创建结果
   */
  def createInstance(chatInfo: ChatInfo, storyId: Long): Future[Boolean] = db.run {
    Story.filter(_.id === storyId).result.headOption
  }.flatMap {
    case Some(story) => db.run {
      StoryInstance.map(r => (r.storyId, r.config, r.chatType, r.chatId)) += ((
        storyId, story.defaultConfig, chatInfo.chatType, chatInfo.chatId
      ))
    } map (_ == 1)
    case None => Future.successful(false)
  }

  /**
   * 查询当前会话中的实例
   *
   * @param chatInfo 会话属性
   * @param storyId  故事 ID
   * @return 查询结果
   */
  def instanceInSession(chatInfo: ChatInfo, storyId: Long): Future[Option[Tables.StoryInstanceRow]] = db.run {
    chatInfo match {
      case ChatInfo(chatType, chatId) =>
        StoryInstance.filter(it => it.chatId === chatId && it.chatType === chatType && it.storyId === storyId)
          .result.headOption
    }
  }

  /**
   * 结束对应实例
   *
   * @param instanceId 实例 ID
   * @return 终止结果
   */
  def terminateInstance(instanceId: Long): Future[Boolean] = db.run {
    StoryInstance.filter(_.id === instanceId).delete
  }.map(_ == 1)

  /**
   * 游戏实例自动存档
   *
   * @param instanceId 实例 ID
   * @param saveFile   存档数据
   * @return 保存结果
   */
  def autoSave(instanceId: Long, saveFile: SaveFile): Future[Boolean] = db.run {
    StoryInstance.filter(_.id === instanceId).map(r => (r.autoSave, r.data)).update((saveFile.choices, saveFile.data))
  }.map(_ == 1)

  /**
   * 游戏实例自动刷新（保存）数据
   *
   * @param instanceId 实例 ID
   * @param data       实例数据
   * @return 保存结果
   */
  def autoRefreshData(instanceId: Long, data: Option[String]): Future[Boolean] = db.run {
    StoryInstance.filter(_.id === instanceId).map(_.data).update(data)
  }.map(_ == 1)

  /**
   * 存档
   *
   * @param storyId  故事 ID
   * @param chatInfo 会话属性
   * @param saveFile 存档
   * @return 保存结果
   */
  def saveStoryStatus(storyId: Long, chatInfo: ChatInfo, saveFile: SaveFile): Future[Boolean] = {
    chatInfo match {
      case ChatInfo(chatType, chatId) => db.run {
        StorySaveFile.filter(it => it.storyId === storyId && it.chatId === chatId && it.chatType === chatType)
          .map(r => (r.id, r.saveList)).result.headOption
      }.flatMap {
        case Some((id, saveList)) => // 存在存档记录，添加
          db.run {
            StorySaveFile.filter(_.id === id).map(_.saveList).update(SaveFile.writeList(SaveFile.readAsList(saveList) :+ saveFile))
          }
        case None => // 不存在存档记录，创建存档记录
          db.run {
            StorySaveFile.map(r => (r.chatId, r.chatType, r.storyId, r.saveList)) += ((
              chatId, chatType, storyId, SaveFile.writeList(List(saveFile))
            ))
          }
      }.map(_ == 1)
    }
  }

  /**
   * 获取全部存档
   *
   * @param storyId  故事 ID
   * @param chatInfo 会话属性
   * @return 全部存档信息
   */
  def saveFileList(storyId: Long, chatInfo: ChatInfo): Future[List[SaveFile]] = {
    chatInfo match {
      case ChatInfo(chatType, chatId) => db.run {
        StorySaveFile.filter(it => it.storyId === storyId && it.chatId === chatId && it.chatType === chatType)
          .map(_.saveList).result.headOption
      }.map(_.map(SaveFile.readAsList).getOrElse(Nil))
    }
  }

  /**
   * 清理升级故事产生的遗留关系数据
   * <p>
   * 如果这是一个遗留故事，并且没有任何对应的 instance 和 save 存在，则清理继承关系和故事数据
   * </p>
   * @return 清理结果
   */
  def storyCleanup(): Future[Unit] = {
    val query = sql"""select distinct s.id
            from public.story s
                left join public.story_instance si on s.id = si.story_id
                left join public.story_save_file ssf on s.id = ssf.story_id
            where status = '遗留'
              and si.id is null
              and ssf.id is null""".as[Long]
    db.run(query).flatMap(needCleanup => db.run {
      DBIO.seq(
        StorySaveInherit.filter(_.legacyStoryId.inSet(needCleanup)).delete,
        Story.filter(_.id.inSet(needCleanup)).delete
      )
    })
  }
}
