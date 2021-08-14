package o.lartifa.jam.plugins.rss

import cc.moecraft.logger.{HyLogger, LogLevel}
import com.apptastic.rssreader.Item
import io.reactivex.rxjava3.core.{Observable, Scheduler}
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.common.util.{MasterUtil, TimeUtil}
import o.lartifa.jam.database.temporary.Memory.database.db
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.plugins.rss.PrettyRSSPrinters.PrettyRSSPrinter
import o.lartifa.jam.plugins.rss.RSSSubscription.{getSourceUrl, logger, scheduler}
import o.lartifa.jam.pool.JamContext

import java.util.Optional
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, TimeUnit}
import scala.util.Try

/**
 * RSS 订阅对象
 *
 * Author: sinar
 * 2020/8/26 21:06
 */
class RSSSubscription(val source: String, private var _channelName: String, val sourceCategory: String, prettyRSSPrinter: PrettyRSSPrinter,
                      private var _subscribers: Set[ChatInfo]) {

  import o.lartifa.jam.database.temporary.Memory.database.profile.api._

  private val errorTimes: AtomicInteger = new AtomicInteger(0)
  private var subscription: Option[Disposable] = None
  private val sourceUrl: String = getSourceUrl(source)

  def channel: String = _channelName

  /**
   * 创建并立刻订阅
   * 只会订阅一次，可重复调用
   *
   * @return 可取消对象
   */
  def subscribeNow(lastKey: String = "IS_NOT_A_KEY"): RSSSubscription = {
    if (subscription.isEmpty) {
      val disposable =
        Observable.interval(1, TimeUnit.MINUTES, scheduler)
          .map(_ => tryReadNext())
          .flatMap(Observable.fromOptional(_))
          .distinct(_.key)
          .skipWhile(_.key == lastKey)
          .subscribe { (item: Item) =>
            logger.debug(s"源${source}更新，正在推送消息")
            _channelName = item.getChannel.getTitle
            val message = prettyRSSPrinter(item)
            logger.debug(s"推送信息预览：$message")
            val output = JamContext.httpApi.get()()
            _subscribers.foreach { case ChatInfo(chatType, id) =>
              // TODO 发送失败时重试
              chatType match {
                case MessageType.PRIVATE => output.sendPrivateMsg(id, message)
                case MessageType.GROUP => output.sendGroupMsg(id, message)
                case MessageType.DISCUSS => output.sendDiscussMsg(id, message)
                case _ => logger.warning(s"未知的聊天类型：$chatType")
              }
            }
            logger.debug(s"源${source}推送完成")
            recordHistory(item.key)
          }
      subscription = Some(disposable)
    }
    this
  }

  /**
   * 获取当前订阅者
   *
   * @return 当前订阅者列表
   */
  def subscribers: Set[ChatInfo] = this._subscribers

  /**
   * 添加订阅者
   *
   * @param newSubscriber 订阅者
   */
  def addSubscriber(newSubscriber: ChatInfo): Set[ChatInfo] = {
    this._subscribers = this._subscribers + newSubscriber
    this._subscribers
  }

  /**
   * 删除订阅者
   *
   * @param subscriber 订阅者
   */
  def removeSubscriber(subscriber: ChatInfo): Set[ChatInfo] = {
    this._subscribers = this._subscribers - subscriber
    this._subscribers
  }

  /**
   * 立刻取消订阅
   *
   * @return 取消状态，false 代表订阅尚未激活，仅属于编码错误
   *         Left(Throwable) 代表取消订阅时出错
   *         true 代表正确状态
   */
  def unsubscribeNow(): Either[Throwable, Boolean] = {
    val subscription = this.subscription.getOrElse(return Right(false))
    Try {
      subscription.dispose()
      this.subscription = None
    }.recover(err => {
      this._subscribers = Set.empty
      MasterUtil.notifyAndLog(s"退订失败，已删除全部订阅者，订阅源为：$source", LogLevel.ERROR, Some(err))
      MasterUtil.notifyMaster("%s，如果频繁出现这个提示，可以上报该问题")
    }).map(_ => true).toEither
  }

  /**
   * 尝试从订阅源中读取下一条消息
   *
   * @return 读取结果
   */
  private def tryReadNext(): Optional[Item] = {
    Try(rss.read(sourceUrl).limit(1).findFirst()).recover(_ => {
      if (errorTimes.incrementAndGet() == 5) {
        MasterUtil.notifyAndLog(s"RSS订阅组件获取订阅源数据失败五次，频道为：$channel，地址为：$sourceUrl")
        MasterUtil.notifyMaster(s"%s，当前订阅该源的聊天/群如下：\n${_subscribers.mkString(",\n")}")
        errorTimes.set(0)
      }
      Optional.empty[Item]()
    }).getOrElse(Optional.empty[Item]())
  }

  /**
   * 记录历史记录
   *
   * @param key 消息键
   */
  private def recordHistory(key: String): Unit = {
    db.run {
      RssSubscription
        .filter(_.source === source)
        .map(row => (row.lastKey, row.lastUpdate))
        .update(key, TimeUtil.currentTimeStamp)
    }
  }
}

object RSSSubscription {

  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(RSSSubscription.getClass)

  private lazy val scheduler: Scheduler = Schedulers.from(Executors.newFixedThreadPool(50))
  /**
   * 获取源地址
   *
   * @param source 源名称
   * @return 源地址
   */
  private[rss] def getSourceUrl(source: String): String = if (RSSConfig.selfDeployedUrl.nonEmpty) {
    if (RSSConfig.selfDeployedUrl.endsWith("/")) s"${RSSConfig.selfDeployedUrl}$source"
    else s"${RSSConfig.selfDeployedUrl}/$source"
  } else s"https://rsshub.app/$source"

  def apply(source: String, channel: String, sourceCategory: String, subscribers: Set[ChatInfo] = Set.empty): RSSSubscription = {
    new RSSSubscription(source, channel, sourceCategory, PrettyRSSPrinters.getByCategory(sourceCategory), subscribers)
  }

  /**
   * 创建并启动（用于快速从数据库恢复订阅）
   *
   * @param record 数据库记录
   * @return 订阅结果对
   */
  def applyAndStart(record: RssSubscriptionRow): (String, RSSSubscription) = {
    val subscription = new RSSSubscription(record.source, record.channel,
      record.sourceCategory, PrettyRSSPrinters.getByCategory(record.sourceCategory),
      record.subscribers.split(",").map(ChatInfo.apply).toSet).subscribeNow(record.lastKey)
    record.source -> subscription
  }
}
