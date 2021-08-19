package o.lartifa.jam.cool.qq.listener

import akka.actor.{ActorRef, Props}
import o.lartifa.jam.pool.JamContext

/**
 * 交互式会话 包对象
 *
 * Author: sinar
 * 2021/8/19 01:13
 */
package object interactive {
  val manager: ActorRef = JamContext.actorSystem.actorOf(Props(InteractiveSessionManager), "interactive-session-manager")
}
