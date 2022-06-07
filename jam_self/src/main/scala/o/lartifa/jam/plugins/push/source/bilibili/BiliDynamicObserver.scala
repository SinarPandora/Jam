package o.lartifa.jam.plugins.push.source.bilibili

import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.database.schema.Tables.SourceObserverRow
import o.lartifa.jam.plugins.push.observer.SourceObserver
import o.lartifa.jam.plugins.push.template.{SourceContent, TemplateRender}
import o.lartifa.jam.pool.ThreadPools

import java.time.format.DateTimeFormatter
import java.util.{Base64, Locale}
import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success}

/**
 * B站动态观察者
 * sourceIdentity 为 UID
 *
 * Author: sinar
 * 2022/6/7 01:04
 */
class BiliDynamicObserver(initData: SourceObserverRow) extends SourceObserver(initData) {
  private val uid: Long = source.sourceIdentity.toLong
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

  /**
   * 拉取消息
   *
   * @return 拉取结果
   */
  override def pull(): Future[Option[SourceContent]] = async {
    BiliClient.Dynamic.getUserDynamic(uid).headOption match {
      case Some(dynamic) =>
        val messageKey = dynamic.dynamicId.toString
        val renderResult = if (TemplateRender.isRendered(source, messageKey)) {
          TemplateRender.render(source, messageKey)
        } else {
          val imageData = await(pullImages(dynamic.face ++: dynamic.pictures.take(9)))
          val templateData = Map(
            "timestamp" -> formatter.format(dynamic.timestamp.toInstant),
            "username" -> dynamic.uname,
            "pictures" -> imageData.tail.asJava,
            "content" -> s"${dynamic.content}${
              if (dynamic.pictures.sizeIs > 9) "\n<最多显示九张图片>" else ""
            }",
            "avatar" -> imageData.head,
          )
          TemplateRender.render(source, messageKey, templateData)
        }
        renderResult match {
          case Failure(exception) =>
            MasterUtil.notifyAndLogError(s"%s，渲染订阅消息过程中出现错误，订阅源：$source, 消息唯一标识：$messageKey", exception)
            None
          case Success(result) => Some(SourceContent(messageKey, result))
        }
      case None => None
    }
  }(ThreadPools.SCHEDULE_TASK)

  /**
   * 拉取全部图片
   *
   * @return 图片数据
   */
  def pullImages(imageUrls: Seq[String])(implicit ec: ExecutionContext = ThreadPools.SCHEDULE_TASK): Future[Seq[String]] = async {
    val tasks = Future.sequence(imageUrls.map(url => Future {
      requests.get(url).bytes
    }(ThreadPools.NETWORK)))
    await(tasks).map(Base64.getEncoder.encodeToString)
  }
}
