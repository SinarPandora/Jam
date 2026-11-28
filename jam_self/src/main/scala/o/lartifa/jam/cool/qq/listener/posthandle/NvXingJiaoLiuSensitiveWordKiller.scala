package o.lartifa.jam.cool.qq.listener.posthandle

import o.lartifa.jam.common.util.TriBoolValue
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.onebot.cq.Cq
import o.lartifa.jam.onebot.domain.{ChatMessage, GroupMessage}
import o.lartifa.jam.plugins.nv_xing_jiao_liu.SensitiveWordKiller
import o.lartifa.jam.pool.JamContext

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

/**
 * 女性♂交流同人功能
 * 在群里抓取聊天内容并分析拼音，如果存在敏感词则每个词禁言一分钟
 *
 * Author: sinar
 * 2024/11/24 21:26
 */
object NvXingJiaoLiuSensitiveWordKiller extends PostHandleTask("女性♂交流", TriBoolValue.Both) {
  private val Day_29_Seconds = 2_505_600
  private val QQAt: Regex = "\\[CQ:at,qq=(?<qq>[0-9]+)]".r

  /**
   * 执行
   *
   * @param event      消息对象
   * @param contextOpt 执行上下文
   * @param exec       异步上下文
   * @return 异步返回执行结果
   */
  override def execute(event: ChatMessage, contextOpt: Option[CommandExecuteContext])(implicit exec: ExecutionContext): Future[Unit] =
    event match {
      case message: GroupMessage =>
        JamContext.variablePool.get(s"Group_${message.groupId}_NXJL_Enabled").flatMap { result =>
          if (result.exists(_.toBoolean)) {
            val mentions = QQAt.findAllMatchIn(event.message)
              .map(_.group("qq"))
              .map(it => JamContext.apiClient.getGroupMemberInfo(message.groupId, it.toLong).getData.getCard)
              .mkString
            val words = SensitiveWordKiller.matching(mentions + event.message)
            if (words.nonEmpty) {
              val now = System.currentTimeMillis()
              var reply = s"${Cq.At(event.senderId)}\n⚠️发现：${words.mkString("，")}${if (words.sizeIs > 1) s"\n${words.size}连击！" else ""}"
              JamContext.variablePool.get(s"Group_${message.groupId}_NXJL_${message.senderId}_ACTIVE").map(_.map(_.toLong)).flatMap { lastActive =>
                lastActive match {
                  case Some(timestamp) =>
                    if (TimeUnit.MILLISECONDS.toMinutes(now - timestamp) < 2) {
                      val banTime = if (words.size >= 22) Day_29_Seconds else 60 * math.pow(2, words.size).toInt
                      JamContext.apiClient.setGroupBan(message.groupId, message.senderId, banTime)
                      reply += s"\n屡教不改！\n本功能由「言阅姬」提供"
                    } else {
                      reply += "\n「好不知廉耻！！」\n本功能由「言阅姬」提供"
                    }
                  case None => reply += "\n你没有注意到吗\n本功能由「言阅姬」提供"
                }
                event.respond(reply)
                JamContext.variablePool.updateOrElseSet(s"Group_${message.groupId}_NXJL_${message.senderId}_ACTIVE", now.toString).map(_ => ())
              }
            } else Future.unit
          } else Future.unit
        }
      case _ => Future.unit
    }
}
