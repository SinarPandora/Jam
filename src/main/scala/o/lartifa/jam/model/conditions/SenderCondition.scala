package o.lartifa.jam.model.conditions

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}
import o.lartifa.jam.common.exception.VarNotFoundException
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.conditions.SenderCondition.Info

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 获取发送者信息进行判断
 *
 * Author: sinar
 * 2020/1/4 17:52
 */
case class SenderCondition(info: Info, value: String, isValueAParam: Boolean = false) extends Condition {

  val getInfo: EventMessage => String = (info: @unchecked) match {
    case SenderCondition.NICKNAME => {
      case message: EventGroupOrDiscussMessage =>
        message.getGroupSender.getInfo.getNickname
      case message: EventPrivateMessage =>
        message.getSender.getInfo.getNickname
    }
    case SenderCondition.QID => _.getSender.getId.toString
    case SenderCondition.AGE => _.getSender.getInfo.getAge.toString
    case SenderCondition.SEX => _.getSender.getInfo.getSex
  }

  /**
   * 是否匹配该种情况
   *
   * @param context 指令执行上下文
   * @param exec    异步执行上下文
   * @return 匹配结果
   */
  override def isMatched(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Boolean] = async {
    val info = getInfo(context.eventMessage)
    val value = if (isValueAParam) {
      await(context.vars.get(this.value)).getOrElse(throw VarNotFoundException(this.value))
    } else this.value
    info == value
  }
}

object SenderCondition {

  sealed class Info

  case object NICKNAME extends Info

  case object QID extends Info

  case object AGE extends Info

  case object SEX extends Info

  object Constant {
    val NICKNAME: String = "昵称"
    val QID: String = "QQ号"
    val AGE: String = "年龄"
    val SEX: String = "性别"
  }

}
