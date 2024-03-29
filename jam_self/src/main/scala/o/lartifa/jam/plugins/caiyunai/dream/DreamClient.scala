package o.lartifa.jam.plugins.caiyunai.dream

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.JSONConfig.formats
import o.lartifa.jam.pool.JamContext
import org.json4s.JValue
import org.json4s.JsonDSL.*
import org.json4s.jackson.JsonMethods.*
import requests.Session

/**
 * 彩云小梦客户端
 * * 虽然彩云小梦可以支持连续多次书写，
 * 但此处只需要完成单次书写加联想
 *
 * Author: sinar
 * 2021/2/20 17:23
 */
object DreamClient {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(DreamClient.getClass)
  private val commonHeader: Map[String, String] = Map(
    "Origin" -> "https://if.caiyunai.com",
    "Referer" -> "https://if.caiyunai.com/dream/",
    "Content-Type" -> "application/json;charset=UTF-8"
  )

  /**
   * 发送短信验证码
   *
   * @param phoneNumber 手机号码
   * @param session     当前会话
   * @return 发送结果（发送成功时返回 codeid）
   */
  def sendCaptcha(phoneNumber: String)(implicit session: Session): Either[String, String] = {
    try {
      val resp = parse {
        session.post(API_V2.sendCaptcha, headers = commonHeader, data = compact {
          ("type" -> "login") ~
            ("phone" -> phoneNumber) ~
            ("callcode" -> 86) ~
            ("uid" -> "") ~
            ("ostype" -> "") ~
            ("lang" -> "zh") ~
            ("User-Agent" -> "1231312313")
        }).text()
      }
      Right((resp \ "data" \ "codeid").extract[String])
    } catch {
      case e: Exception =>
        logger.error(e)
        Left("验证码发送失败，请一分钟后重试")
    }
  }


  /**
   * 手机登录
   *
   * @param phoneNumber 手机号
   * @param code        验证码
   * @param codeId      验证码所有者标识
   * @param session     当前会话
   * @return 登录结果（成功时返回 uid）
   */
  def phoneLogin(phoneNumber: String, code: String, codeId: String)(implicit session: Session): Either[String, String] = {
    try {
      val resp = parse {
        session.post(API_V2.phoneLogin, headers = commonHeader, data = compact {
          ("code" -> code) ~
            ("phone" -> phoneNumber) ~
            ("codeid" -> codeId) ~
            ("uid" -> "") ~
            ("callcode" -> 86) ~
            ("ostype" -> "") ~
            ("lang" -> "zh") ~
            ("User-Agent" -> "1231312313")
        }).text()
      }
      Right((resp \ "data" \ "_id").extract[String])
    } catch {
      case e: Exception =>
        logger.error(e)
        Left("登录失败，请检查验证码是否正确")
    }
  }

  /**
   * 小梦 AI 模型
   *
   * @param name 模型名称
   * @param mid  模型 ID
   */
  case class AICharacter(name: String, mid: String)

  /**
   * 获取小梦模型
   *
   * @param session 当前会话
   * @return 可用的小梦 AI 模型列表
   */
  def listModels(implicit session: Session): Either[String, List[AICharacter]] = {
    try {
      val resp = parse(session.get(API_V2.modelList, headers = commonHeader).text())
      Right {
        resp.extract[Seq[JValue]].flatMap { v =>
          val name = (v \ "name").extract[String].trim
          val mid = (v \ "_id").extract[String].trim
          if (name != "" && mid != "") Some(AICharacter(name, mid)) else None
        }.toList
      }
    } catch {
      case e: Exception =>
        logger.error(e)
        Left("获取小梦模型时失败，请稍后重试")
    }
  }

  case class NovelMetadata(nid: String, lastNode: String, branchId: String, firstNode: String)

  /**
   * 初次保存
   *
   * @param uid     用户 ID
   * @param content 内容
   * @param session 当前会话
   * @return 成功时返回：小说 ID
   */
  def saveAtFirst(uid: String, content: String)(implicit session: Session): Either[String, NovelMetadata] = {
    try {
      val resp = parse {
        session.post(API_V2.novelSave(uid), headers = commonHeader, data = compact {
          ("title" -> "") ~
            ("nodes" -> List()) ~
            ("text" -> content) ~
            ("ostype" -> "") ~
            ("lang" -> "zh") ~
            ("User-Agent" -> "1231312313")
        }).text()
      }
      Right(NovelMetadata(
        nid = (resp \ "data" \ "novel" \ "nid").extract[String],
        lastNode = (resp \ "data" \ "novel" \ "lastnode").extract[String],
        branchId = (resp \ "data" \ "novel" \ "branchid").extract[String],
        firstNode = (resp \ "data" \ "novel" \ "firstnode").extract[String]
      ))
    } catch {
      case e: Exception =>
        logger.error(e)
        Left("保存内容时失败，请稍后重试")
    }
  }

  case class Dream(content: String, xid: String, nodeId: String, parentId: String)

  /**
   * 小云做梦（AI 联想）
   *
   * @param uid      用户 ID
   * @param mid      模型 Id
   * @param content  内容
   * @param metadata 小说元数据
   * @param session  会话
   * @return 全部梦境
   */
  def dreaming(uid: String, mid: String, content: String, metadata: NovelMetadata)(implicit session: Session): Either[String, List[Dream]] = {
    try {
      metadata match {
        case NovelMetadata(nid, lastNode, branchId, _) =>
          val resp = parse {
            session.post(API_V2.novelAI(uid), headers = commonHeader, data = compact {
              ("nid" -> nid) ~
                ("content" -> content) ~
                ("uid" -> uid) ~
                ("mid" -> mid) ~
                ("title" -> "") ~
                ("status" -> "http") ~
                ("lastnode" -> lastNode) ~
                ("branchid" -> branchId) ~
                ("ostype" -> "") ~
                ("lang" -> "zh") ~
                ("User-Agent" -> "1231312313")
            }).text()
          }
          Right((resp \ "data" \ "nodes").extract[Seq[JValue]].map(node => {
            Dream(
              content = (node \ "content").extract[String],
              xid = (node \ "xid").extract[String],
              nodeId = (node \ "nodeid").extract[String],
              parentId = (node \ "parentid").extract[String])
          }).toList)
      }
    } catch {
      case e: Exception =>
        logger.error(e)
        Left("无法获取联想结果，请稍后重试")
    }
  }
}
