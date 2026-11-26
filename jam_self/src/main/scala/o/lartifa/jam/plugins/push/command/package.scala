package o.lartifa.jam.plugins.push

import o.lartifa.jam.database.Memory.database.*
import o.lartifa.jam.database.Memory.database.profile.api.*
import o.lartifa.jam.database.schema.Tables
import o.lartifa.jam.database.schema.Tables.*
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.pool.ThreadPools

import scala.concurrent.{ExecutionContext, Future}

/**
 * 通用方法
 * Author: sinar
 * 2022/6/13 23:36
 */
package object command {
  /**
   * 获取源观察者
   *
   * @param sourceType     源类型
   * @param sourceIdentity 源标识
   * @return 源观察者（可能存在）
   */
  private[command] def getObserver(sourceType: String, sourceIdentity: String): Future[Option[Tables.SourceObserverRow]] = db.run {
    SourceObserver.filter(row => row.sourceType === sourceType && row.sourceIdentity === sourceIdentity)
      .result
      .headOption
  }

  /**
   * 获取源订阅者
   *
   * @param sourceType     源类型
   * @param sourceIdentity 源标识
   * @param chatInfo       会话信息
   * @return 源订阅者（可能存在）
   */
  private[command] def getSubscriber(sourceType: String, sourceIdentity: String, chatInfo: ChatInfo): Future[Option[SourceSubscriberRow]] = {
    implicit val exec: ExecutionContext = ThreadPools.DB
    val ChatInfo(chatType, chatId) = chatInfo
    getObserver(sourceType, sourceIdentity).flatMap {
      case Some(observer) =>
        db.run {
          SourceSubscriber
            .filter(row => row.sourceId === observer.id && row.chatId === chatId && row.chatType === chatType)
            .result.headOption
        }
      case None => Future.successful(None)
    }
  }
}
