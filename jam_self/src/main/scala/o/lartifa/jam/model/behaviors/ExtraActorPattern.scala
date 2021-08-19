package o.lartifa.jam.model.behaviors

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, Props}
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{Future, Promise}

/**
 * Extra Actor 模式
 *
 * Author: sinar
 * 2021/8/9 00:02
 */
trait ExtraActorPattern {
  /**
   * 通过 extra actor 发送和接收消息
   *
   * @param msg 消息内容
   * @param ref 目标 actor
   * @tparam T 消息类型
   * @return 异步结果（因为类型擦除，此处返回 Any）
   */
  protected def sendViaActor[T](msg: T, ref: ActorRef): Future[Any] = {
    val promise = Promise[Any]()
    JamContext.actorSystem.actorOf(Props(new Actor {
      override def preStart(): Unit = ref ! msg

      override def receive: Receive = {
        case any => promise.success(any)
      }
    }))
    promise.future
  }

  /**
   * 通过 extra actor 发送和处理消息
   *
   * @param msg 消息内容
   * @param ref 目标 actor
   * @param f   消息处理函数
   * @tparam T 消息类型
   */
  protected def handleViaActor[T](msg: T, ref: ActorRef)(f: Receive): Unit = {
    JamContext.actorSystem.actorOf(Props(new Actor {
      override def preStart(): Unit = ref ! msg

      override def receive: Receive = f
    }))
  }
}

object ExtraActorPattern {
  sealed trait Event

  case object Timeout extends Event
}

