package o.lartifa.jam.cool.qq.listener

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.onebot.domain.{FriendAddRequest, GroupInviteRequest, MetaEvent, MetaHeartbeat, MetaLifecycle, RequestEvent}
import o.lartifa.jam.pool.JamContext

import scala.util.Try

/**
 * 系统事件监听器：request / meta 由 OneBotInbound 直喂。
 *
 * Author: sinar
 * 2020/1/18 02:59
 */
object SystemEventListener {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(SystemEventListener.getClass)

  /**
   * 处理热路径请求事件（好友申请 / 群邀请）
   */
  def handleRequestEvent(event: RequestEvent): Unit = {
    event match {
      case _: GroupInviteRequest =>
        if (JamConfig.config.autoAcceptGroupRequest) {
          event.accept()
        }
      case _: FriendAddRequest =>
        if (JamConfig.config.autoAcceptFriendRequest) {
          event.accept()
        }
    }
    Option(JamContext.bot.get()).foreach { bot =>
      Try(bot.refreshCache()).failed.foreach { err =>
        logger.debug(s"请求处理后刷新缓存失败：${err.getMessage}")
      }
    }
  }

  /**
   * 处理元事件：心跳刷新账号缓存；lifecycle 本阶段 no-op。
   */
  def handleMetaEvent(event: MetaEvent): Unit =
    event match {
      case _: MetaHeartbeat =>
        logger.debug("正在刷新账号缓存")
        Option(JamContext.bot.get()).foreach { bot =>
          Try(bot.refreshCache()).foreach { _ =>
            logger.debug("缓存刷新成功")
          }
        }
      case _: MetaLifecycle =>
        ()
    }
}
