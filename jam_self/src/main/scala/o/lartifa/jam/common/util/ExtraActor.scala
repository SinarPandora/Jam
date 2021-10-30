package o.lartifa.jam.common.util

import akka.actor.Actor
import akka.util.Timeout
import o.lartifa.jam.pool.ThreadPools

import scala.concurrent.duration.FiniteDuration

/**
 * Extra Actor
 * * 接收到任何一条消息后直接退出
 *
 * Author: sinar
 * 2021/9/8 00:02
 */
abstract class ExtraActor(timeout: Option[FiniteDuration] = None) extends Actor {

  /**
   * 启动时执行
   * 若设置了超时，此处会设置超时信息
   */
  final override def preStart(): Unit = {
    timeout.foreach(timeout => {
      context.system.scheduler.scheduleOnce(timeout) {
        self ! Timeout(timeout)
      }(ThreadPools.DEFAULT)
    })
    onStart()
  }

  /**
   * 当接收到消息时执行
   * 当收到任意一条消息后立刻停止
   *
   * @return 接收消息行为函数
   */
  final override def receive: Receive = {
    case any =>
      handle(any)
      context.stop(self)
  }

  // 启动时操作
  def onStart(): Unit = {}

  // 处理消息
  def handle: Receive
}
