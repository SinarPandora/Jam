package o.lartifa.jam.plugins.push.source.bilibili

import o.lartifa.jam.common.util.{MasterUtil, ResUtil}
import o.lartifa.jam.plugins.push.scanner.SourceScanner
import o.lartifa.jam.plugins.push.source.SourceIdentity
import o.lartifa.jam.plugins.push.source.bilibili.BiliClient.Dynamic.EmojiInfo
import o.lartifa.jam.plugins.push.template.{SourceContent, TemplateRender}
import o.lartifa.jam.pool.ThreadPools

import java.time.format.DateTimeFormatter
import java.util
import java.util.Locale
import scala.async.Async.{async, await}
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success}

/**
 * Bili动态扫描器
 *
 * Author: sinar
 * 2022/6/13 22:14
 */
object BiliDynamicSourceScanner extends SourceScanner {
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

  /**
   * 源扫描
   *
   * @param identity 源标识
   * @param ec       异步上下文
   * @return 扫描结果
   */
  override def scan(identity: SourceIdentity)(implicit ec: ExecutionContext): Future[Option[SourceContent]] = async {
    val SourceIdentity(_, uid) = identity
    BiliClient.Dynamic.getUserDynamic(uid.toLong).headOption match {
      case Some(dynamic) =>
        val messageKey = dynamic.dynamicId.toString
        val renderResult = if (TemplateRender.isRendered(identity, messageKey)) {
          TemplateRender.render(identity, messageKey)
        } else {
          val tasks = await {
            Future.sequence(Seq(
              pullImages(dynamic.face +: dynamic.pictures.take(3)),
              convertImageInContent(dynamic.content, dynamic.emojiInfo)
            ))
          }
          val imageData = tasks.head.asInstanceOf[Seq[String]]
          val content = tasks.last.asInstanceOf[String]
          val data = new util.HashMap[String, Object]()
          data.put("timestamp", formatter.format(dynamic.timestamp.toLocalDateTime))
          data.put("username", dynamic.uname)
          data.put("pictures", imageData.tail.asJava)
          data.put("content", s"$content${if (dynamic.pictures.sizeIs > 3) "\n<最多显示三张图片>" else ""}")
          data.put("avatar", imageData.head)
          TemplateRender.render(identity, messageKey, data)
        }
        renderResult match {
          case Failure(exception) =>
            MasterUtil.notifyAndLogError(s"%s，渲染订阅消息过程中出现错误，订阅源：$identity, 消息唯一标识：$messageKey", exception)
            None
          case Success(result) => Some(SourceContent(messageKey, result))
        }
      case None => None
    }
  }

  /**
   * 拉取全部图片
   *
   * @return 图片数据
   */
  def pullImages(imageUrls: Seq[String])(implicit ec: ExecutionContext = ThreadPools.SCHEDULE_TASK): Future[Seq[String]] = async {
    val tasks = Future.sequence(imageUrls.map(url => Future {
      ResUtil.downloadPicToHTMLBase64(url)
    }(ThreadPools.NETWORK)))
    await(tasks).map(_.base64)
  }

  /**
   * 转换内容中的 emoji
   *
   * @param content   内容
   * @param emojiInfo emoji 信息
   * @param ec        异步上下文
   */
  def convertImageInContent(content: String, emojiInfo: EmojiInfo)(implicit ec: ExecutionContext = ThreadPools.SCHEDULE_TASK): Future[String] = async {
    val emojiURL = ListBuffer[String]()
    val rawMap: Map[String, Int] = emojiInfo.zipWithIndex.map {
      case ((k, v), i) =>
        emojiURL += v
        k -> i
    }.toMap
    val emojiData = await(pullImages(emojiURL.toSeq)).zipWithIndex.map {
      case (data, idx) => idx -> data
    }.toMap
    rawMap.foldLeft(content) {
      case (content, (text, idx)) =>
        val html = s"""<img src="${emojiData(idx)}" style="max-width: 20px" alt="$text"/>"""
        content.replaceAll(text.replace("[", "\\["), html)
    }
  }
}
