package o.lartifa.jam.bionic

import cn.hutool.cron.CronUtil

import scala.concurrent.{ExecutionContext, Future}

/**
 * 仿生行为初始化器
 *
 * Author: sinar
 * 2020/1/23 14:54 
 */
object BehaviorInitializer {
  /**
   * 初始化
   */
  def init()(implicit exec: ExecutionContext): Future[Unit] = Future {
    CronUtil.start()
  }
}
