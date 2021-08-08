package o.lartifa.jam.plugins.trpg.mixin

import akka.actor.ActorRef
import akka.pattern.ask
import glokka.Registry
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.pool.JamContext

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}

/**
 * 在当前的会话中查找 TRPG 游戏
 *
 * Author: sinar
 * 2021/7/25 01:29
 */
trait FindGameInSession {
  /**
   * 会话中的游戏引用
   *
   * @param chatInfo 会话信息
   * @return 游戏引用
   */
  def gameRef(chatInfo: ChatInfo): Option[ActorRef] = {
    Await.result(JamContext.registry.ask(Registry.Lookup(s"trpg_game_$chatInfo"))(5.seconds), Duration.Inf) match {
      case Registry.Found(_, ref) => Some(ref)
      case Registry.NotFound(_) => None
    }
  }
}
