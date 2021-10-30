package o.lartifa.jam.model.behaviors

import akka.actor.{ActorRef, Props}
import o.lartifa.jam.pool.JamContext

/**
 * Actor 创建者
 *
 * Author: sinar
 * 2021/8/22 03:47
 */
trait ActorCreator {
  /**
   * 创建一个 Actor
   *
   * @param actorProto actor 定义
   */
  def actorOf(actorProto: Props): ActorRef = JamContext.actorSystem.actorOf(actorProto)
}

object ActorCreator extends ActorCreator
