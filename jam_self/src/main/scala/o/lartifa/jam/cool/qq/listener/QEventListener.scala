package o.lartifa.jam.cool.qq.listener

import cc.moecraft.icq.event.events.notice.{EventNotice, EventNoticeFriendPoke, EventNoticeGroupPoke}
import cc.moecraft.icq.event.{EventHandler, IcqListener}
import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import cn.hutool.core.date.StopWatch
import o.lartifa.jam.common.config.BotConfig
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.cool.qq.listener.base.Break
import o.lartifa.jam.cool.qq.listener.event.{CQEvent, PokeEvent, PokeInGroupEvent}
import o.lartifa.jam.model.patterns.ContentMatcher
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.pool.JamContext

import scala.concurrent.Future
import scala.util.Random

/**
 * CQ 事件监听器
 *
 * Author: sinar
 * 2021/7/4 11:49
 */
object QEventListener extends IcqListener {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(QEventListener.getClass)
  /**
   * 监听全部通知事件
   *
   * @param event 通知事件
   */
  @EventHandler
  def handleNoticeEvent(event: EventNotice): Unit = {
    if (!JamContext.initLock.get()) {
      val evt = event match {
        case evt: EventNoticeFriendPoke => PokeEvent(evt)
        case evt: EventNoticeGroupPoke => PokeInGroupEvent(evt)
        case _ => return // 其他事件直接忽略
      }
      handleEvent(evt)
    }
  }

  /**
   * 分发事件
   *
   * @param event 翻译后的事件对象
   */
  private def handleEvent(event: CQEvent): Unit = {
    implicit val context: CommandExecuteContext = CommandExecuteContext(event)
    if (!BanList.isAllowed(event.chatInfo)) return
    buildSearchPath(event.chatInfo).find(matcher => matcher.isMatched(event.name)).foreach {
      case ContentMatcher(stepId, _, _, _) =>
        val handleCost = new StopWatch()
        handleCost.start()
        logger.log(s"已捕获${event.name}事件，开始处理")
        JamContext.stepPool.get().goto(stepId).recover {
          case break: Break =>
            logger.log(s"事件捕获被打断，退出码为：${break.exitCode}")
          case exception =>
            logger.error(exception)
            MasterUtil.notifyMaster(s"%s，步骤${stepId}执行失败了，原因是：${exception.getMessage}")
        }.flatMap(_ => {
          handleCost.stop()
          val cost = handleCost.getTotalTimeSeconds
          if (cost < 1) logger.log(s"${AnsiColor.GREEN}事件${event.name}处理完毕！执行步骤ID：$stepId，耗时：小于1s")
          else logger.log(s"${AnsiColor.RED}事件${event.name}处理完毕！步骤ID：$stepId，耗时：${cost}s")
          Future.unit
        })
    }
  }

  /**
   * 构建事件搜索路径
   *
   * @param chatInfo 当前会话信息
   * @return 搜索路径
   */
  private def buildSearchPath(chatInfo: ChatInfo): List[ContentMatcher] = {
    import EvtMatchers.*
    val customMatchers = custom.get().get(chatInfo.chatType).flatMap(_.get(chatInfo.chatId)).getOrElse(List.empty)
    val chatTypeScopeMatchers = (chatInfo.chatType match {
      case MessageType.PRIVATE => globalPrivate
      case MessageType.GROUP | MessageType.DISCUSS => globalGroup
    }).get()
    if (BotConfig.matchOutOfOrder) {
      Random.shuffle(customMatchers) ++ Random.shuffle(chatTypeScopeMatchers) ++ Random.shuffle(global.get())
    } else {
      customMatchers ++ chatTypeScopeMatchers ++ global.get()
    }
  }
}
