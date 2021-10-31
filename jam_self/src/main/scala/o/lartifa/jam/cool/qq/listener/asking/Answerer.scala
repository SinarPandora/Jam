package o.lartifa.jam.cool.qq.listener.asking

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.cool.qq.listener.asking.Questioner.QuestionContext
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, _}

/**
 * 问答者对象
 *
 * 当其被模式匹配时，含义如下
 * case Answerer(Some(qID), Some(groupID)) =>
 * // 群聊并匹配个人
 * case Answerer(Some(qID), None) =>
 * // 私聊
 * case Answerer(None, Some(groupID)) =>
 * // 群聊并匹配所有人
 * case Answerer(None, None) =>
 * // 不应存在的情况
 *
 * Author: sinar
 * 2020/9/18 21:45
 */
@deprecated(since = "3.1", message = "Please use o.lartifa.jam.cool.qq.listener.interactive")
sealed case class Answerer(qID: Option[Long], groupID: Option[Long]) {

  /**
   * 询问
   *
   * @param options  期待回答（默认表示接受一切回应）
   * @param times    询问次数（与期待回答结合使用，表示当答案没命中预期时，再进行几次提问）
   * @param timeout  回答超时（过期自动取消提问，默认两分钟）
   * @param callback 回调（当命中期待回答时执行），当回调返回 false 时，
   */
  def ?(options: Set[String] = Set.empty, times: Int = 1,
        timeout: Option[Duration] = Some(2.minutes))
       (callback: QuestionContext => Future[Result]): Unit =
    Questioner.ask(this, options, times, timeout)(callback)

  /**
   * 询问
   *
   * @param callback 回调（当命中期待回答时执行）
   */
  def ?(callback: QuestionContext => Future[Result]): Unit =
    Questioner.ask(this)(callback)
}

object Answerer {
  /**
   * 询问会话中的任何人
   *
   * @param eventMessage 消息对象
   * @return 回答者
   */
  def anyInThisSession(eventMessage: EventMessage): Answerer = {
    eventMessage match {
      case message: EventGroupOrDiscussMessage =>
        new Answerer(None, Some(message.getGroup.getId))
      case message: EventPrivateMessage =>
        new Answerer(Some(message.getSenderId), None)
    }
  }

  /**
   * 询问会话中的任何人
   *
   * @param context 执行上下文
   * @return 回答者
   */
  def anyInThisSession(implicit context: CommandExecuteContext): Answerer =
    anyInThisSession(context.eventMessage)

  /**
   * 询问（或获取）当前的发言人
   *
   * @param eventMessage 消息对象
   * @return 回答者
   */
  def sender(eventMessage: EventMessage): Answerer = {
    eventMessage match {
      case message: EventGroupOrDiscussMessage =>
        new Answerer(Some(message.getSenderId), Some(message.getGroup.getId))
      case message: EventPrivateMessage =>
        new Answerer(Some(message.getSenderId), None)
    }
  }

  /**
   * 询问（或获取）当前的发言人
   *
   * @param context 执行上下文
   * @return 回答者
   */
  def sender(implicit context: CommandExecuteContext): Answerer =
    sender(context.eventMessage)

  def apply(qID: Option[Long], groupID: Option[Long]): Answerer = {
    if (qID.isEmpty && groupID.isEmpty) {
      throw ExecutionException("创建了无意义的回答者对象")
    } else new Answerer(qID, groupID)
  }
}
