package o.lartifa.jam.common.util

import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.onebot.domain.{ChatMessage, DiscussMessage, GroupMessage, PrivateMessage}
import o.lartifa.jam.pool.JamContext

import scala.util.Try

/**
 * Event message 扩展
 *
 * Author: sinar
 * 2020/10/18 23:16
 */
object EventMessageHelper {

  implicit class Helper(event: ChatMessage) {
    /**
     * 获取发送者昵称
     * 若存在群昵称则优先获取群昵称
     * 否则使用 QQ 昵称替代
     *
     * @return 昵称
     */
    def senderNickName: String = getNickname(false)

    /**
     * 获取 bot(自身) 昵称
     * 若存在群昵称则优先获取群昵称
     * 否则使用 QQ 昵称替代
     *
     * @return 昵称
     */
    def selfNickname: String = getNickname(true)

    /**
     * 获取发送者 QQ 昵称
     *
     * @return QQ 昵称
     */
    def senderQQName: String = getQQNickName(false)

    /**
     * 获取 bot(自身) QQ 昵称
     *
     * @return QQ 昵称
     */
    def selfQQName: String = getQQNickName(true)

    /**
     * 构造会话对象
     *
     * @return 会话对象
     */
    def chatInfo: ChatInfo = ChatInfo(event)

    /**
     * 获取昵称（通用）
     *
     * @param isSelf 是否为发送者
     * @return 昵称
     */
    private def getNickname(isSelf: Boolean): String = {
      event match {
        case _: GroupMessage | _: DiscussMessage => getGroupNickName(isSelf)
        case _: PrivateMessage => getQQNickName(isSelf)
      }
    }

    /**
     * 获取昵称
     *
     * @param isSelf 是否为发送者
     * @return 昵称
     */
    private def getQQNickName(isSelf: Boolean): String = {
      if (isSelf) JamConfig.config.name
      else event.sender.nickname.filter(_.nonEmpty).getOrElse(event.senderId.toString)
    }

    /**
     * 获取群昵称，当未设置时，获取昵称
     *
     * @param isSelf 是否为发送者
     * @return 群昵称
     */
    private def getGroupNickName(isSelf: Boolean): String = {
      val groupNickName =
        if (isSelf) Try(JamContext.apiClient.getGroupMemberInfo(toGroupMessage.groupId, event.selfId).getData.getCard)
          .toOption.flatMap(Option(_)).getOrElse("")
        else event.sender.card.getOrElse("")
      if (groupNickName.isEmpty) getQQNickName(isSelf)
      else groupNickName
    }

    /**
     * 尝试将消息转换成群消息
     *
     * @return 群消息
     */
    @throws[ExecutionException]
    def toGroupMessage: GroupMessage =
      Try(event.asInstanceOf[GroupMessage])
        .getOrElse(throw ExecutionException("该消息非群聊消息"))

    /**
     * 是否发言者是管理员
     *
     * @return 是否为管理员
     */
    def isSenderManager: Boolean = {
      event match {
        case _: GroupMessage | _: DiscussMessage => event.sender.isAdmin
        case _ => false
      }
    }
  }

}
