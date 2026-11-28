package o.lartifa.jam.onebot

import o.lartifa.jam.onebot.protocol.api.BotApi

import java.util.concurrent.atomic.AtomicReference
import scala.util.Try

/**
 * 自有薄启动壳：替代 PicqBotX。持 BotApi、好友/群 ID 缓存与 notice 去重开关。
 *
 * Author: sinar
 * 2026/11/28
 */
final class JamBot(
  val name: String,
  val httpClient: OneBotHttpClient,
  val api: BotApi,
  val multiAccountOptimizations: Boolean = true,
) {
  private val friendIdsRef = new AtomicReference[Set[Long]](Set.empty)
  private val groupIdsRef = new AtomicReference[Set[Long]](Set.empty)

  def friendIds: Set[Long] = friendIdsRef.get()
  def groupIds: Set[Long] = groupIdsRef.get()

  /**
   * 拉取好友/群列表填入缓存（对齐旧 AccountManager.refreshCache）。
   */
  def refreshCache(): Unit = {
    val friends = Try(api.getFriendList().map(_.userId).toSet).getOrElse(Set.empty)
    val groups = Try(api.getGroupList().map(_.groupId).toSet).getOrElse(Set.empty)
    friendIdsRef.set(friends)
    groupIdsRef.set(groups)
  }
}
