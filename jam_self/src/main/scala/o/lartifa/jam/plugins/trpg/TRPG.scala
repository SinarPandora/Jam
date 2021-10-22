package o.lartifa.jam.plugins.trpg

import akka.actor.Actor
import o.lartifa.jam.common.protocol.{Done, Fail}
import o.lartifa.jam.model.behaviors.{ActorCreator, ReplyToFriend}
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.plugins.trpg.TRPGGameManager.TRPGManage

/**
 * TRPG 工具
 *
 * Author: sinar
 * 2021/7/17 23:07
 */
object TRPG extends ActorCreator with ReplyToFriend {
  def loadGame() = ???

  def exitGameInSession(chatInfo: ChatInfo)(implicit ctx: CommandExecuteContext): Unit = {
    // TODO save game

    // Exit game
    actorOf(new Actor {

      override def preStart(): Unit = {
        gameManager ! TRPGManage.Release(chatInfo, self)
      }

      override def receive: Receive = {
        case Done =>
          reply("游戏已保存并退出")
          context.stop(self)
        case Fail(msg) =>
          reply(msg)
          context.stop(self)
      }
    })
  }

}
