package o.lartifa.jam.model.behaviors

import akka.actor.{Actor, ActorRef, Props}
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
  def actorOf(actorProto: Actor): ActorRef = JamContext.actorSystem.actorOf(Props(actorProto))
}

object ActorCreator extends ActorCreator
