package o.lartifa.jam.common.util

import cc.moecraft.icq.event.events.message.{EventGroupMessage, EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.ChatInfo

import scala.util.Try

/**
 * Event message 扩展
 *
 * Author: sinar
 * 2020/10/18 23:16
 */
object EventMessageHelper {

  implicit class Helper(event: EventMessage) {
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
        case _: EventGroupOrDiscussMessage => getGroupNickName(isSelf)
        case _: EventPrivateMessage => getQQNickName(isSelf)
      }
    }

    /**
     * 获取昵称
     *
     * @param isSelf 是否为发送者
     * @return 昵称
     */
    private def getQQNickName(isSelf: Boolean): String = {
      if (isSelf) event.getBotAccount.getName
      else Try(event.getSender.refreshInfo(true).getNickname)
        .getOrElse(event.getSenderId.toString)
    }

    /**
     * 获取群昵称，当未设置时，获取昵称
     *
     * @param isSelf 是否为发送者
     * @return 群昵称
     */
    private def getGroupNickName(isSelf: Boolean): String = {
      val qid = if (isSelf) event.getSelfId else event.getSenderId
      val groupNickName = toGroupMessage.getGroupUser(qid).refreshInfo.getCard
      if (groupNickName.isEmpty) getQQNickName(isSelf)
      else groupNickName
    }

    /**
     * 尝试将消息转换成群消息
     *
     * @return 群消息
     */
    @throws[ExecutionException]
    def toGroupMessage: EventGroupMessage =
      Try(event.asInstanceOf[EventGroupMessage])
        .getOrElse(throw ExecutionException("该消息非群聊消息"))
  }

}
