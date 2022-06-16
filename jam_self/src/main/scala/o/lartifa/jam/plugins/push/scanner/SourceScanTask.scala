package o.lartifa.jam.plugins.push.scanner

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.PluginConfig
import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.common.util.TimeUtil
import o.lartifa.jam.database.Memory.database.*
import o.lartifa.jam.database.Memory.database.profile.api.*
import o.lartifa.jam.database.schema.Tables.*
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.model.tasks.JamCronTask
import o.lartifa.jam.plugins.push.scanner.SourceScanTask.{Subscriber, logger}
import o.lartifa.jam.plugins.push.source.SourceIdentity
import o.lartifa.jam.plugins.push.template.{RenderResult, SourceContent}
import o.lartifa.jam.pool.{JamContext, ThreadPools}

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}
import scala.io.AnsiColor
import scala.util.{Failure, Success}

/**
 * 源扫描任务
 *
 * Author: sinar
 * 2022/6/12 22:14
 */
class SourceScanTask(name: String) extends JamCronTask(name) {

  /**
   * 执行定时任务内容
   *
   * @return 并发占位符
   */
  override def run()(implicit exec: ExecutionContext): Future[Unit] = async {
    val query = db.run {
      SourceObserver
        .join(SourceSubscriber)
        .on(_.id === _.sourceId)
        .filter {
          case (obs, sub) => !obs.isPaused && !sub.isPaused
        }
        .map {
          case (obs, sub) => (obs.sourceType, obs.sourceIdentity, sub.id, sub.chatType, sub.chatId, sub.lastKey)
        }
        .result
    }
    val sourceInfo: Map[SourceIdentity, Seq[Subscriber]] = await(query)
      .map {
        case (sourceType, sourceIdentity, id, chatType, chatId, lastKey) =>
          val identity = SourceIdentity(sourceType, sourceIdentity)
          identity -> Subscriber(id, ChatInfo(chatType, chatId), lastKey, identity)
      }
      .groupMap(_._1)(_._2)
    sourceInfo.foreach {
      case (identity@SourceIdentity(sourceType, _), subscribers) =>
        SourceScanner.scanners.get(sourceType).foreach { scanner => {
          scanner
            .scan(identity)(ThreadPools.NETWORK)
            .filter(_.nonEmpty)
            .map(_.get)
            .onComplete {
              case Failure(exception) => logger.error(exception)
              case Success(content) =>
                subscribers.foreach { subscriber =>
                  createPushTask(content, subscriber)
                }
            }
        }
        }
    }
  }


  /**
   * 创建推送任务
   *
   * @param content    源内容
   * @param subscriber 订阅者数据
   * @param ec         异步上下文
   * @return 推送任务
   */
  def createPushTask(content: SourceContent, subscriber: Subscriber)(implicit ec: ExecutionContext = ThreadPools.SCHEDULE_TASK): Unit = {
    val SourceContent(messageKey, RenderResult(message)) = content
    val Subscriber(id, chatInfo, lastKey, identity) = subscriber
    async {
      if (messageKey != lastKey) {
        val pushed = await(db.run {
          SourcePushHistory
            .filter(row => row.subscriberId === id && row.messageKey === messageKey)
            .exists
            .result
        })
        if (!pushed) {
          import ChatInfo.ChatInfoReply
          chatInfo.sendMsg(message)
          await(db.run {
            DBIO.sequence(Seq(
              SourceSubscriber
                .filter(_.id === id)
                .map(row => (row.lastKey, row.lastUpdateTime))
                .update((messageKey, TimeUtil.currentTimeStamp)),
              SourcePushHistory.map(row => (row.subscriberId, row.messageKey))
                += ((id, messageKey))
            ))
          })
          true
        } else false
      } else false
    } onComplete {
      case Failure(exception) =>
        logger.error(s"推送出现错误，消息标识：$messageKey，消息内容：$message，消息源：$identity，目标聊天：$chatInfo", exception)
      case Success(isPushed) =>
        if (isPushed) logger.debug(s"消息成功推送，消息标识：$messageKey，消息内容：$message，消息源：$identity，目标聊天：$chatInfo")
    }
  }
}

object SourceScanTask {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(classOf[SourceScanTask])

  /**
   * 初始化源扫描任务
   */
  def init(): Unit = {
    logger.log(s"${AnsiColor.GREEN}[源订阅] ${AnsiColor.YELLOW}正在初始化")
    JamContext.cronTaskPool.get().getActiveTasks(SourceScanTask.name).left
      .getOrElse(throw ParseFailException("源扫描任务尚未初始化"))
      .setUp(s"*/${PluginConfig.config.sourcePush.scanFrequency} * * * *")
    logger.log(s"${AnsiColor.GREEN}[源订阅] 初始化成功，已设定为每 ${PluginConfig.config.sourcePush.scanFrequency} 分钟扫描一次")
  }

  val name: String = "源扫描"
  case class Subscriber(id: Long, chatInfo: ChatInfo, lastKey: String, identity: SourceIdentity)
}

