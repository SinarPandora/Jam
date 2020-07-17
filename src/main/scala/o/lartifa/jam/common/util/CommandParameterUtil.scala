package o.lartifa.jam.common.util

import cc.moecraft.icq.event.events.message.{EventGroupMessage, EventMessage}
import o.lartifa.jam.common.exception.ExecutionException

import scala.util.Try

/**
 * 指令参数工具
 *
 * Author: sinar
 * 2020/7/17 21:08
 */
object CommandParameterUtil {

  /**
   * 创建获取参数值的函数
   *
   * @param from         参数容器
   * @param eventMessage 消息事件
   * @return 函数
   */
  def createGetParamFunc(from: String => Option[String], eventMessage: EventMessage): String => String = {
    case "昵称" => eventMessage.getBotAccount.getName
    case "群昵称" => toGroupMessage(eventMessage).getGroupUser(eventMessage.getSenderId).getInfo.getNickname
    case "发送者昵称" | "对方昵称" => eventMessage.getSender.getInfo.getNickname
    case "发送者群昵称" | "对方群昵称" => toGroupMessage(eventMessage).getGroupSender.getInfo.getNickname
    case "是否为好友" => if (toGroupMessage(eventMessage).getGroupSender.getInfo.getUnfriendly) "是" else "否"
    case other => from(other).getOrElse(throw ExecutionException(s"没有找到名为 $other 的变量"))
  }

  /**
   * 尝试将消息转换成群消息
   *
   * @param eventMessage 消息事件
   * @return 群消息
   */
  @throws[ExecutionException]
  private def toGroupMessage(eventMessage: EventMessage): EventGroupMessage =
    Try(eventMessage.asInstanceOf[EventGroupMessage])
      .getOrElse(throw ExecutionException("不能从非群聊获取群昵称"))
}
