package o.lartifa.jam.cool.qq.listener.asking

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, ScheduledExecutorService, ScheduledFuture, TimeUnit}

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.cool.qq.listener.asking.Question.{logger, questionTimer}
import o.lartifa.jam.cool.qq.listener.asking.Questioner.QuestionContext
import o.lartifa.jam.cool.qq.listener.listenerCommonPool
import o.lartifa.jam.pool.JamContext

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
 * 待解决的问题
 *
 * Author: sinar
 * 2020/9/18 21:44
 */
case class Question(hit: (EventMessage, Answerer) => Boolean, answerer: Answerer,
                    times: AtomicInteger = new AtomicInteger(1),
                    timeout: Option[Duration],
                    private val callback: QuestionContext => Future[Result],
                    private val targetQueue: ListBuffer[Question]) {
  val task: Option[ScheduledFuture[Runnable]] = timeout.map(duration => {
    questionTimer.schedule(() => {
      targetQueue.synchronized {
        targetQueue -= this
      }
    }.asInstanceOf[Runnable], duration.toSeconds, TimeUnit.SECONDS)
  })

  /**
   * 回答
   *
   * @param eventMessage 命中的回复消息
   * @param answerer     回答者
   */
  def answerBy(eventMessage: EventMessage, answerer: Answerer): Future[Result] = {
    callback(QuestionContext(answerer, eventMessage, this)).recover(err => {
      logger.error(err)
      MasterUtil.notifyMaster(s"%，在处理回答时出现问题，消息内容为：${eventMessage.message}")
      Result.Complete
    }).andThen(_ => {
      task.foreach(_.cancel(false))
    })
  }
}

object Question {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(Question.getClass)
  private val questionTimer: ScheduledExecutorService = Executors.newScheduledThreadPool(4)
}
