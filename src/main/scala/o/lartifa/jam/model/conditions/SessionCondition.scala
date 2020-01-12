package o.lartifa.jam.model.conditions

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}
import o.lartifa.jam.common.exception.ParamNotFoundException
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.conditions.SessionCondition.Info

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 获取会话信息进行判断
 *
 * Author: sinar
 * 2020/1/4 17:52 
 */
case class SessionCondition(info: Info, value: String, isValueAParam: Boolean = false) extends Condition {

  val getInfo: EventMessage => String = (info: @unchecked) match {
    case SessionCondition.TYPE => {
      case _: EventGroupOrDiscussMessage => "群"
      case _: EventPrivateMessage => "私人"
    }
    case SessionCondition.QID => {
      case message: EventPrivateMessage => message.getSenderId.toString
      case message: EventGroupOrDiscussMessage => message.getGroup.getId.toString
    }
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
      val pool = context.variablePool
      await(pool.get(this.value)).getOrElse(throw ParamNotFoundException(this.value))
    } else this.value
    info == value
  }
}

object SessionCondition {

  sealed class Info

  case object QID extends Info

  case object TYPE extends Info

  object Constant {
    val TYPE: String = "类型"
    val QID: List[String] = List("QQ号", "群号")
  }

}
