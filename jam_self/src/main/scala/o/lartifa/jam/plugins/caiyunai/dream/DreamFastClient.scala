package o.lartifa.jam.plugins.caiyunai.dream

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.pool.JamContext
import requests.Session

import java.util.concurrent.TimeUnit
import scala.annotation.tailrec
import scala.async.Async._
import scala.concurrent.Future

/**
 * 彩云小梦
 * 快速回复客户端
 *
 * Author: sinar
 * 2021/6/12 22:19
 */
object DreamFastClient {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(DreamFastClient.getClass)

  private case class Connection(uid: String, mid: String, sign: String)

  /**
   * 使用彩云小梦引擎对内容进行单词联想
   *
   * @param content 消息内容
   * @return 联想结果
   */
  def reply(content: String): Future[Option[String]] = {
    implicit val session: Session = requests.Session()
    initConnection()
      .map {
        case Some(value) => value
        case None =>
          logger.warning("无法建立与彩云小梦服务器的连接，若此问题连续出现多次，请关闭后置任务：联想回复")
          throw ExecutionException("无法建立与彩云小梦服务器的连接")
      }
      .flatMap(fastDream(_, content))
  }

  /**
   * 初始化连接
   *
   * @param session 消息会话
   * @return 彩云小梦连接
   */
  private def initConnection()(implicit session: Session): Future[Option[Connection]] = async {
    val uidFu = async(DreamClient.getUid)
    val modelsFu = async(DreamClient.listModels)
    val signFu = async(DreamClient.getSignature)
    val uidOpt = await(uidFu).toOption
    val midOpt = await(modelsFu).map(_.headOption).toOption.flatten
    val signOpt = await(signFu).toOption
    if (uidOpt.isEmpty || midOpt.isEmpty || signOpt.isEmpty) None
    else Some(Connection(uid = uidOpt.get, mid = midOpt.get.mid, sign = signOpt.get))
  }

  /**
   * 快速做梦（指联想）
   *
   * @param connection 彩云小梦连接信息
   * @param content    消息内容
   * @param session    会话
   * @return 联想内容
   */
  private def fastDream(connection: Connection, content: String)(implicit session: Session): Future[Option[String]] = {
    connection match {
      case Connection(uid, mid, _) =>
        Future {
          DreamClient.save("", content, uid) match {
            case Left(_) =>
              logger.warning("彩云小梦保存失败，若此问题连续出现多次，请关闭后置任务：联想回复")
              throw ExecutionException("彩云小梦保存失败")
            case Right(nid) => nid
          }
        }.map { nid =>
          DreamClient.dream("", content, uid, nid, mid) match {
            case Left(_) =>
              logger.warning("彩云小梦无法建立梦境，若此问题连续出现多次，请关闭后置任务：联想回复")
              throw ExecutionException("彩云小梦入梦失败")
            case Right(xid) =>
              fastLooping(uid, nid, xid)
          }
        }
    }
  }

  /**
   * 快速梦境循环
   * 注意：该方法为同步方法，且只联想十次（每次 3 秒）以避免过长等待
   *
   * @param uid     用户 ID
   * @param nid     小说 ID
   * @param xid     梦境 ID
   * @param count   循环次数
   * @param session 会话
   * @return 第一条梦境内容
   */
  @tailrec
  private def fastLooping(uid: String, nid: String, xid: String, count: Int = 1)(implicit session: Session): Option[String] = {
    DreamClient.dreamLoop(uid, nid, xid) match {
      case Left(_) =>
        logger.warning("彩云小梦梦境循环失败，若此问题连续出现多次，请关闭后置任务：联想回复")
        throw ExecutionException("彩云小梦梦境循环失败")
      case Right(resp) =>
        if (resp.isEmpty) {
          if (count > 10) None
          else {
            TimeUnit.SECONDS.sleep(3)
            fastLooping(uid, nid, xid, count + 1)
          }
        } else resp.headOption.map(_.content)
    }
  }
}
