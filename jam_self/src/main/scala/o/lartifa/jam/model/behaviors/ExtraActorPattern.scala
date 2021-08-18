package o.lartifa.jam.model.behaviors

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, Props}
import o.lartifa.jam.common.exception.ExecutionException
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
   * @tparam R 接收消息类型
   * @return 异步结果
   */
  protected def sendViaActor[T, R](msg: T, ref: ActorRef): Future[R] = {
    val promise = Promise[R]()
    JamContext.actorSystem.actorOf(Props(new Actor {
      override def preStart(): Unit = ref ! msg

      override def receive: Receive = {
        case rtn: R => promise.success(rtn)
        case other => promise.failure(ExecutionException(s"收到消息类型不匹配：${other.getClass.toString}"))
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

