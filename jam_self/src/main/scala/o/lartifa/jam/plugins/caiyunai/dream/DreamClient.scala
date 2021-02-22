package o.lartifa.jam.plugins.caiyunai.dream

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.pool.JamContext
import requests.Session

import scala.util.{Failure, Try}

/**
 * 彩云小梦客户端
 *
 * Author: sinar
 * 2021/2/20 17:23
 */
object DreamClient {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(DreamClient.getClass)

  /**
   * 获取 UID
   *
   * @return UID
   * @param session 当前会话
   */
  def getUid(implicit session: Session): Either[String, String] = {
    Try(session.post(APIs.getUid, data = "{\"ostype\":\"\"}").text())
      .recoverWith(err => {
        logger.error(err)
        Failure(err)
      })
      .map(ujson.read(_)("data")("user")("_id").str)
      .map(Right.apply)
      .getOrElse(Left("获取 UID 失败"))
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
    Try(session.post(APIs.listModels, data = "{\"ostype\":\"\"}").text())
      .map(ujson.read(_)("data")("public_rows").arr.flatMap(v => {
        val name = v("name").str.trim
        val mid = v("mid").str.trim
        if (name != "" && mid != "") Some(AICharacter(name, mid)) else None
      }).toList)
      .recoverWith(err => {
        logger.error(err)
        Failure(err)
      })
      .map(Right.apply)
      .getOrElse(Left("获取小梦模型时失败"))
  }

  /**
   * 获取签名
   *
   * @param session 当前会话
   * @return 彩云小梦 APP 签名
   */
  def getSignature(implicit session: Session): Either[String, String] = {
    Try(session.post(APIs.getSignature, data = "{\"url\":\"http://if.caiyunai.com/dream/\",\"ostype\":\"\"}").text())
      .recoverWith(err => {
        logger.error(err)
        Failure(err)
      })
      .map(Right.apply)
      .getOrElse(Left("获取签名时失败"))
  }

  /**
   * 保存编写内容
   *
   * @param title   标题
   * @param content 内容
   * @param uid     用户 ID（必填）
   * @param nid     小说 ID（选填）
   * @param session 当前会话
   * @return 保存成功时返回 UID（文章 ID)
   */
  def save(title: String, content: String, uid: String, nid: Option[String] = None)(implicit session: Session): Either[String, String] = {
    val data = ujson.Obj("content" -> content, "title" -> title, "ostype" -> "")
    nid.foreach(id => data("nid") = id)
    Try(session.post(APIs.save(uid), data = data).text())
      .recoverWith(err => {
        logger.error(err)
        Failure(err)
      })
      .map(ujson.read(_)("data")("nid").str)
      .map(Right.apply)
      .getOrElse(Left("保存失败，请稍后再试"))
  }

  /**
   * 小云做梦（触发AI续写）
   *
   * @param title   标题
   * @param content 内容
   * @param uid     用户 ID
   * @param nid     小说 ID
   * @param mid     模型 ID
   * @param session 当前会话
   * @return 保存成功时返回 XID（梦境 ID）
   */
  def dream(title: String, content: String, uid: String, nid: String, mid: String)(implicit session: Session): Either[String, String] = {
    Try(session.post(APIs.dream(uid), data =
      ujson.Obj("nid" -> nid, "content" -> content, "uid" -> uid,
        "mid" -> mid, "title" -> title, "ostype" -> "")).text())
      .recoverWith(err => {
        logger.error(err)
        Failure(err)
      })
      .map(ujson.read(_)("data")("xid").str)
      .map(Right.apply)
      .getOrElse(Left("AI 联想启动失败，请稍后再试"))
  }

  /**
   * 小云梦境
   *
   * @param idx     选项编号
   * @param content 联想内容
   * @param xid     梦境 ID
   */
  case class Dream(idx: Int, content: String, xid: String)

  /**
   * 梦境回环（获取续写内容）
   *
   * @param uid     用户 ID
   * @param nid     小说 ID
   * @param xid     梦境 ID
   * @param session 当前会话
   * @return 成功时返回梦境列表
   */
  def dreamLoop(uid: String, nid: String, xid: String)(implicit session: Session): Either[String, List[Dream]] = {
    Try(session.post(APIs.dreamLoop(uid), data = s"""{"nid":"$nid","xid":"$xid","ostype":""}""").text())
      .map(ujson.read(_)("data")("rows").arr.zipWithIndex.map {
        case (v, i) => Dream(i, v("content").str.trim, v("_id").str.trim)
      }.toList)
      .recoverWith(err => {
        logger.error(err)
        Failure(err)
      })
      .map(Right.apply)
      .getOrElse(Left("无法获取联想结果，请稍后重试"))
  }

  /**
   * 稳定故事线（将续写内容添加到文本）
   *
   * @param uid     用户 ID
   * @param xid     梦境 ID
   * @param index   选项编号
   * @param session 当前会话
   * @return 成功时返回 true
   */
  def realizingDream(uid: String, xid: String, index: Int)(implicit session: Session): Either[String, Boolean] = {
    Try(session.post(APIs.realizingDream(uid), data =
      s"""{
         |  "xid": "$xid",
         |  "index": $index,
         |  "ostype": ""
         |}""".stripMargin).text())
      .recoverWith(err => {
        logger.error(err)
        Failure(err)
      })
      .map(ujson.read(_)("msg").str.trim == "ok")
      .map(Right.apply)
      .getOrElse(Left("保存梦境结果时失败，请稍后重试"))
  }
}
