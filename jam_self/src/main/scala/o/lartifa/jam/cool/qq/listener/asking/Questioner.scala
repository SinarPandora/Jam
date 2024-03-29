package o.lartifa.jam.cool.qq.listener.asking

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}
import o.lartifa.jam.cool.qq.listener.listenerCommonPool

import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, _}

/**
 * 消息询问者
 *
 * Author: sinar
 * 2020/9/18 19:48
 */
@deprecated(since = "3.1", message = "Please use o.lartifa.jam.cool.qq.listener.interactive")
object Questioner {

  case class QuestionContext(answerer: Answerer, event: EventMessage, question: Question)

  /**
   * 待解决问题队列
   * 为了内存安全，此处在设置了队列后，即使队列在回答后被清空也不会删除它
   */
  private val questionWaitingQueue: mutable.Map[Answerer, ListBuffer[Question]] = mutable.Map.empty

  /**
   * 尝试让发送者回答已存在的问题
   *
   * @param eventMessage 消息对象
   * @return true：继续处理之后的消息
   *         false：中断，并等待下一条消息
   */
  def tryAnswerer(eventMessage: EventMessage): Future[Boolean] = {
    val currentAnswer = Answerer.sender(eventMessage)
    if (questionWaitingQueue.isEmpty) return Future.successful(true)
    val questInSession = eventMessage match {
      case _: EventGroupOrDiscussMessage =>
        questionWaitingQueue.get(currentAnswer)
          .orElse(questionWaitingQueue.get(currentAnswer.copy(qID = None)))
          .getOrElse(return Future.successful(true))
      case _: EventPrivateMessage =>
        questionWaitingQueue.getOrElse(currentAnswer, return Future.successful(true))
    }
    questInSession.synchronized {
      if (questInSession.isEmpty) return Future.successful(true)
      val question = questInSession.find(q => q.hit(eventMessage, currentAnswer)).getOrElse(return Future.successful(true))
      question.answerBy(eventMessage, currentAnswer).map {
        case Result.KeepCountAndContinueAsking =>
          false
        case Result.ContinueAsking =>
          if (question.times.decrementAndGet() == 0) {
            questInSession -= question
          }
          false
        case Result.AskAgainLater =>
          // Move to tail
          questInSession -= question
          questInSession += question
          false
        case Result.Complete =>
          questInSession -= question
          false
        case Result.CompleteThenParseMessage =>
          questInSession -= question
          true
      }
    }
  }

  /**
   * 询问
   *
   * @param options  期待回答（默认表示接受一切回应）
   * @param times    询问次数（与期待回答结合使用，表示当答案没命中预期时，再进行几次提问）
   * @param answerer 只聆听此人（默认接受一切回复者）
   * @param timeout  回答超时（过期自动取消提问，默认两分钟）
   * @param callback 回调（当命中期待回答时执行）
   */
  def ask(answerer: Answerer, options: Set[String] = Set.empty, times: Int = 1,
          timeout: Option[Duration] = Some(2.minutes))
         (callback: QuestionContext => Future[Result]): Unit = {
    val hit = answerer match {
      case Answerer(Some(_), Some(_)) =>
        (event: EventMessage, _answerer: Answerer) => {
          event match {
            case _: EventGroupOrDiscussMessage =>
              answerer == _answerer &&
                (options.isEmpty || options.contains(event.message.trim))
            case _: EventPrivateMessage => false
          }
        }
      case Answerer(None, Some(groupID)) =>
        (event: EventMessage, _: Answerer) => {
          event match {
            case message: EventGroupOrDiscussMessage =>
              message.getGroup.getId == groupID &&
                (options.isEmpty || options.contains(event.message.trim))
            case _: EventPrivateMessage => false
          }
        }
      case _ =>
        (event: EventMessage, _answerer: Answerer) => {
          event match {
            case _: EventGroupOrDiscussMessage => false
            case _: EventPrivateMessage =>
              answerer == _answerer &&
                (options.isEmpty || options.contains(event.message.trim))
          }
        }
    }
    questionWaitingQueue.synchronized {
      val targetQueue = questionWaitingQueue.getOrElseUpdate(answerer, ListBuffer())
      val quest = Question(hit, answerer, new AtomicInteger(times), timeout, callback, targetQueue)
      targetQueue += quest
    }
  }

  /**
   * 询问（ask 的代理方法）
   *
   * @param options  期待回答（默认表示接受一切回应）
   * @param times    询问次数（与期待回答结合使用，表示当答案没命中预期时，再进行几次提问）
   * @param answerer 只聆听此人（默认接受一切回复者）
   * @param timeout  回答超时（过期自动取消提问，默认两分钟）
   * @param callback 回调（当命中期待回答时执行）
   */
  def ?(answerer: Answerer, options: Set[String] = Set.empty, times: Int = 1,
        timeout: Option[Duration] = Some(2.minutes))
       (callback: QuestionContext => Future[Result]): Unit =
    this.ask(answerer, options, times, timeout)(callback)


  /**
   * 取消对该回答者之后全部的问题
   *
   * @param answerer 回答者
   */
  def cancelAllNext(answerer: Answerer): Unit = {
    val queue = questionWaitingQueue.getOrElse(answerer, return)
    queue.synchronized {
      queue --= queue.filter(it => it.answerer == answerer)
    }
  }
}
