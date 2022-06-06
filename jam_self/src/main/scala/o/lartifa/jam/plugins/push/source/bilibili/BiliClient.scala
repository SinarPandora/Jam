package o.lartifa.jam.plugins.push.source.bilibili

import o.lartifa.jam.common.config.JSONConfig.formats
import org.json4s.*
import org.json4s.jackson.JsonMethods.*

import java.sql.Timestamp

/**
 * Bilibili API Client
 *
 * Author: sinar
 * 2022/4/30 17:34
 */
object BiliClient {
  object Dynamic {
    // Text -> URL
    type EmojiInfo = Map[String, String]
    object Type {
      val FORWARD: Int = 1
      val ALBUM: Int = 2
      val TEXT: Int = 4
      val VIDEO: Int = 8
    }
    case class Dynamic
    (
      dynamicId: Long,
      `type`: Int,
      uid: Long,
      timestamp: Timestamp,
      uname: String,
      face: String,
      content: String,
      pictures: Seq[String],
      cardDecorate: Option[String],
      emojiInfo: EmojiInfo
    )
    case class DynamicContent(uname: String, face: String, content: String, pictures: Seq[String] = Seq.empty)

    /**
     * 通过 UID 获取用户动态（前 20 条）
     *
     * @param uid     用户 ID
     * @param offset  偏移量（默认为 0）
     * @param needTop 是否获取置顶动态（默认为否）
     * @return 用户动态（一次最多 20 条）
     */
    def getUserDynamic(uid: Long, offset: Int = 0, needTop: Boolean = false): Seq[Dynamic] = {
      val resp = parse {
        requests.get(s"https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=$uid&need_top=$needTop&offset_dynamic_id=$offset")
          .text()
      }
      (resp \ "data" \ "cards").extractOrElse[Seq[JValue]](Seq.empty).map(extractDynamic)
    }

    /**
     * 提取动态
     *
     * @param json JSON 值对象
     * @return 提取结果
     */
    private def extractDynamic(json: JValue): Dynamic = {
      val emojiInfo = (json \ "display" \ "emoji_info" \ "emoji_details")
        .extractOrElse[Seq[JValue]](Seq())
        .map(j => (j \ "text").extract[String] -> (j \ "url").extract[String])
        .toMap

      val dynamicCard = parse((json \ "card").extract[String])
      val `type` = (json \ "desc" \ "type").extract[Int]
      val content = extractDynamicContent(`type`, dynamicCard)

      Dynamic(
        dynamicId = (json \ "desc" \ "dynamic_id").extract[Long],
        `type` = `type`,
        uid = (json \ "desc" \ "dynamic_id").extract[Long],
        timestamp = new Timestamp((json \ "desc" \ "timestamp").extract[Long] * 1000),
        uname = content.uname,
        face = content.face,
        content = content.content,
        pictures = content.pictures,
        cardDecorate = (json \ "desc" \ "decorate_card" \ "image_enhance").extractOpt[String],
        emojiInfo = emojiInfo,
      )
    }

    /**
     * 通过 ID 获取动态
     *
     * @param dynamicId 动态 ID
     * @return 动态信息
     */
    private def getDynamic(dynamicId: Long): Option[Dynamic] = {
      val resp = parse {
        requests.get(s"https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail?dynamic_id=$dynamicId")
          .text()
      }
      (resp \ "data" \ "card").extractOpt[JValue].map(extractDynamic)
    }

    /**
     * 提取动态内容
     *
     * @param `type`      动态类型
     * @param dynamicCard 动态卡片 json
     * @return 动态内容
     */
    private def extractDynamicContent(`type`: Int, dynamicCard: JValue): DynamicContent = {
      `type` match {
        case Type.FORWARD => extractForwardDynamic(dynamicCard)
        case Type.ALBUM => extractAlbumDynamic(dynamicCard)
        case Type.TEXT => extractTextDynamic(dynamicCard)
        case Type.VIDEO => extractVideoDynamic(dynamicCard)
        case _ => throw new NoSuchMethodException("")
      }
    }

    /**
     * 提取转发动态信息
     *
     * @param card 动态卡片 json
     * @return 动态内容
     */
    private def extractForwardDynamic(card: JValue): DynamicContent = {
      val oriContent = extractDynamicContent(
        `type` = (card \ "item" \ "orig_type").extract[Int],
        dynamicCard = card \ "origin"
      )
      DynamicContent(
        uname = (card \ "user" \ "uname").extract[String],
        face = (card \ "user" \ "face").extract[String],
        content =
          s"""${(card \ "item" \ "content").extractOrElse[String]("转发动态")}
             |<hr />
             |${oriContent.content}""".stripMargin,
        pictures = oriContent.pictures,
      )
    }

    /**
     * 提取相册动态信息
     *
     * @param card 动态卡片 json
     * @return 动态内容
     */
    private def extractAlbumDynamic(card: JValue): DynamicContent = {
      DynamicContent(
        uname = (card \ "user" \ "name").extract[String],
        face = (card \ "user" \ "head_url").extract[String],
        content = (card \ "item" \ "description").extract[String],
        pictures = (card \ "item" \ "pictures" \\ "img_src").extractOrElse[Seq[String]](Seq.empty)
      )
    }

    /**
     * 提取文字动态信息
     *
     * @param card 动态卡片 json
     * @return 动态内容
     */
    private def extractTextDynamic(card: JValue): DynamicContent = {
      DynamicContent(
        uname = (card \ "user" \ "uname").extract[String],
        face = (card \ "user" \ "face").extract[String],
        content = (card \ "item" \ "content").extract[String]
      )
    }

    /**
     * 提取投稿动态信息
     *
     * @param card 动态卡片 json
     * @return 动态内容
     */
    private def extractVideoDynamic(card: JValue): DynamicContent = {
      DynamicContent(
        uname = (card \ "owner" \ "name").extract[String],
        face = (card \ "owner" \ "face").extract[String],
        content =
          s"""标题：${(card \ "title").extract[String]}
             |简介：${(card \ "desc").extract[String]}""".stripMargin,
        pictures = Seq(
          (card \ "pic").extract[String]
        )
      )
    }
  }

}
