package o.lartifa.jam.plugins.rss

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.Locale
import java.util.regex.Pattern

import cc.moecraft.icq.sender.message.components.ComponentImage
import com.apptastic.rssreader.Item
import o.lartifa.jam.common.util.MasterUtil

import scala.util.Try

/**
 * RSS 消息格式化输出
 *
 * Author: sinar
 * 2020/8/29 01:51
 */

object PrettyRSSPrinters {
  type PrettyRSSPrinter = Item => String

  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MMMdd日（EEEE）HH:mm:ss", Locale.CHINA)

  private val parser: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)

  private val imageRegex: Pattern = Pattern.compile("""https?://[^'"]*?\.(?:png|jpg|gif|jpeg)""",
    Pattern.CASE_INSENSITIVE)

  val RichText: PrettyRSSPrinter = it => {
    val pics = extractImages(it.getDescription.orElse(""))
    val picData = pics.take(3).map(new ComponentImage(_).toString).mkString("\n") +
      (if (pics.sizeIs > 3) "\n为防止刷屏，此处只显示三张" else "")
    s"""${it.getChannel.getTitle}：
       |摘要：${it.getTitle.orElse("无标题")}
       |$picData
       |链接：${it.getLink.orElse("无连接")}
       |${timeFormat(it)}""".stripMargin
  }

  val TitleAndLink: PrettyRSSPrinter = it => {
    s"""${it.getChannel.getTitle}：
       |${it.getTitle.orElse("无标题")}
       |${it.getLink.orElse("无连接")}""".stripMargin
  }

  val Minimal: PrettyRSSPrinter = it => s"""${it.getChannel.getTitle}：${it.getTitle.orElse(it.key)}"""


  private val printers: Map[String, PrettyRSSPrinter] = Map(
    "图文混排" -> RichText,
    "摘要和链接" -> TitleAndLink,
    "仅摘要" -> Minimal
  )

  /**
   * 通过源类别获取输出样式
   *
   * @param sourceCategory 源类别
   * @return 输出样式
   */
  def getByCategory(sourceCategory: String): PrettyRSSPrinter = {
    val name = RSSConfig.customStyleMap.getOrElse(sourceCategory, RSSConfig.defaultStyle)
    printers.get(name) match {
      case Some(value) => value
      case None =>
        MasterUtil.notifyMaster("%s，RSS 样式配置不正确，已经替换为图文混排")
        RichText
    }
  }

  /**
   * 提取字符串中全部的图片
   *
   * @param str 待解析字符串
   * @return 图片列表
   */
  private def extractImages(str: String): Seq[String] = {
    val matcher = imageRegex.matcher(str)
    LazyList.from(1).takeWhile(_ => matcher.find()).map(_ => matcher.group(0))
  }

  /**
   * 转换时间格式
   *
   * @param item RSS 消息
   * @return 格式化后的时间
   */
  private def timeFormat(item: Item): String = Try {
    Option(item.getPubDate.orElse(null))
      .map(it => parser.parse(it))
      .map(Instant.from(_).atZone(ZoneId.systemDefault()))
      .map(it => formatter.format(it))
      .getOrElse("近期更新")
  }.getOrElse("近期更新")
}
