package o.lartifa.jam.plugins.rss

import java.util.stream.Collectors

import cc.moecraft.logger.HyLogger
import com.apptastic.rssreader.Item
import o.lartifa.jam.common.config.SystemConfig
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.database.temporary.Memory.database.db
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.model.behaviors.ReplyToUser
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.pool.JamContext

import scala.async.Async.{async, await}
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.io.AnsiColor
import scala.util.{Failure, Success, Try}

/**
 * 订阅记录池
 *
 * Author: sinar
 * 2020/8/30 13:11
 */
object SubscriptionPool extends ReplyToUser {

  import o.lartifa.jam.database.temporary.Memory.database.profile.api._

  private val rssSubscriptions: mutable.Map[String, RSSSubscription] = mutable.Map.empty
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(SubscriptionPool.getClass)

  /**
   * 在果酱苏醒后重新建立 RSS 订阅
   */
  def init(): Unit = {
    import RSSSubscription.rssRecordPool
    db.run(RssSubscription.result).map(list => {
      rssSubscriptions ++= list.map(RSSSubscription.applyAndStart).toMap
      logger.log(s"${AnsiColor.GREEN}RSS订阅已恢复，已导入${rssSubscriptions.size}个源，监听中...")
    }).onComplete {
      case Failure(exception) =>
        logger.error("从数据库恢复源时出错", exception)
        MasterUtil.notifyMaster("%s，自动恢复订阅失败，目前订阅功能无法使用，请检查数据源是否正常")
      case Success(_) => if (SystemConfig.debugMode) {
        MasterUtil.notifyMaster(s"%s，RSS订阅已恢复，已导入${rssSubscriptions.size}个源")
      }
    }
  }

  /**
   * 订阅 RSS 并回复
   *
   * @param source   订阅源
   * @param chatInfo 会话信息
   * @param isForce  是否为强制模式
   * @param context  指令上下文
   * @param exec     异步上下文
   */
  def subscribeAndReply(source: String, chatInfo: ChatInfo, isForce: Boolean)(implicit context: CommandExecuteContext,
                                                                              exec: ExecutionContext): Future[Unit] = async {
    val _source = source.trim
    if (sourceIsOk(_source, isForce)) {
      synchronized {
        rssSubscriptions.get(_source) match {
          case Some(subscription) => onUpdateCallback(subscription, chatInfo)
          case None => await(createSubscription(_source)).foreach(it => {
            rssSubscriptions += _source -> it
            onUpdateCallback(it, chatInfo)
          })
        }
      }
    }
  }

  /**
   * 退订 RSS 并回复
   *
   * @param source   订阅源
   * @param chatInfo 会话信息
   * @param context  指令上下文
   * @param exec     异步上下文
   */
  def unSubscribeAndReply(source: String, chatInfo: ChatInfo)(implicit context: CommandExecuteContext,
                                                              exec: ExecutionContext): Unit = {
    synchronized {
      val _source = source.trim
      val subscription = rssSubscriptions.getOrElse(_source, return)
      val subscribers = subscription.removeSubscriber(chatInfo)
      if (subscribers.isEmpty) {
        if (!subscription.unsubscribeNow().getOrElse(true)) {
          logger.warning("退订了一个没有被激活的订阅源")
        }
        rssSubscriptions -= _source
        removeSubscription(_source).onComplete(onDeleteCallback(_source))
      }
      else recordSubscription(_source, subscribers).onComplete(onDeleteCallback(_source))
    }
  }

  /**
   * 列出当前会话的订阅信息并回复
   *
   * @param chatInfo 会话信息
   */
  def listSubscriptionsAndReply(chatInfo: ChatInfo)(implicit context: CommandExecuteContext, exec: ExecutionContext): Unit = {
    db.run {
      RssSubscription.filter(_.subscribers like s"%${chatInfo.chatId}%")
        .map(row => (row.channel, row.source, row.lastUpdate)).result
    }.onComplete {
      case Failure(exception) =>
        logger.error("查询订阅时发生错误", exception)
        context.eventMessage.respond("没查出来。。。")
      case Success(list) =>
        if (list.isEmpty) context.eventMessage.respond("当前会话没有订阅任何 RSS 源")
        else {
          context.eventMessage.respond("当前会话订阅内容如下：")
          list.zipWithIndex.map {
            case ((channel, source, lastUpdate), idx) =>
              s"${idx + 1}：$channel，更新于${lastUpdate.formatted("yyyy-MM-dd HH:mm:ss")}，源名称：$source"
          }
            .sliding(5, 5)
            .map(_.mkString(",\n"))
            .foreach(context.eventMessage.respond)
        }
    }
  }

  /**
   * 组合更新订阅回调
   *
   * @param subscription RSS 订阅
   * @param chatInfo     会话信息
   * @param context      指令上下文
   * @param exec         执行上下文
   */
  private def onUpdateCallback(subscription: RSSSubscription, chatInfo: ChatInfo)
                              (implicit context: CommandExecuteContext, exec: ExecutionContext): Unit = {
    val subscribers = subscription.addSubscriber(chatInfo)
    recordSubscription(subscription.source, subscribers).onComplete {
      case Failure(exception) =>
        logger.error(exception)
        context.eventMessage.respond("订阅失败，一会再试试看？")
        MasterUtil.notifyMaster(s"%s，源订阅失败，请查看日志，源名称：${subscription.source}，$chatInfo")
      case Success(_) =>
        context.eventMessage.respond(s"$atSender 订阅成功！")
        subscription.subscribeNow()
    }
  }

  /**
   * 组合删除订阅回调
   *
   * @param source  订阅源
   * @param context 指令上下文
   * @return 回调函数
   */
  private def onDeleteCallback(source: String)(implicit context: CommandExecuteContext): PartialFunction[Try[Int], Any] = {
    case Failure(exception) =>
      logger.error(exception)
      context.eventMessage.respond("退订失败，一会再试试看？")
      MasterUtil.notifyMaster(s"%s，源退订失败，请查看日志，源名称：$source，${context.chatInfo}")
    case Success(count) =>
      if (count == 1) {
        context.eventMessage.respond(s"$atSender 已退订")
      } else {
        logger.warning("订阅记录更新/删除失败")
        context.eventMessage.respond("退订失败，一会再试试看？")
        MasterUtil.notifyMaster(s"%s，源退订失败，请查看日志，源名称：$source，${context.chatInfo}")
      }
  }

  /**
   * 检查源是否可订阅
   *
   * @param source  订阅源
   * @param isForce 是否为强制订阅模式（在源没有返回任何内容时，非'强制订阅模式'将不会订阅该源
   * @param context 指令上下文
   * @return 检查结果
   */
  private def sourceIsOk(source: String, isForce: Boolean)(implicit context: CommandExecuteContext): Boolean = {
    if (source.toLowerCase.startsWith("http")) {
      context.eventMessage.respond("暂时不支持 RSSHUB 之外的源，请使用支持的源，比如：weibo/user/12345")
      false
    } else true
  }

  /**
   * 根据源创建对应订阅
   *
   * @param source  订阅源
   * @param context 指令上下文
   * @param exec    异步上下文
   * @return 订阅对象
   */
  private def createSubscription(source: String)(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Option[RSSSubscription]] = async {
    val sourceCategory = {
      val (prefix, content) = source.splitAt(source.lastIndexOf("/"))
      if (prefix.isEmpty) content
      else prefix
    }
    Try(rss.read(source).limit(1).collect(Collectors.toList())) match {
      case Failure(exception) =>
        logger.error(s"获取源数据时失败，该源可能不属于 RSSHUB：$source", exception)
        context.eventMessage.respond("订阅失败，请检查源是否属于 RSSHUB")
        None
      case Success(item: Item) =>
        val subscription = RSSSubscription(source, sourceCategory)
        await {
          db.run {
            RssSubscription
              .map(row => (row.source, row.channel, row.sourceCategory, row.subscribers)) += (
              source, item.getChannel.getTitle, sourceCategory, "")
          }
        }
        Some(subscription)
    }
  }

  /**
   * 记录订阅信息到数据库
   *
   * @param source 订阅源
   * @param chats  会话信息
   * @return 影响记录数
   */
  private def recordSubscription(source: String, chats: Set[ChatInfo]): Future[Int] = db.run {
    RssSubscription
      .filter(row => row.source === source)
      .map(_.subscribers)
      .update(chats.map(_.serialize).mkString(","))
  }

  /**
   * 从数据库删除订阅信息
   *
   * @param source 订阅源
   * @return 影响记录数
   */
  private def removeSubscription(source: String): Future[Int] = db.run {
    RssSubscription
      .filter(row => row.source === source)
      .delete
  }

}
