package o.lartifa.jam.cool.qq.listener.prehandle

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.cool.qq.listener.prehandle.FlipsRepeatedImage.logger
import o.lartifa.jam.plugins.filppic.{MessageImageUtil, QQImg}
import o.lartifa.jam.pool.JamContext

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * Author: sinar
 * 2020/8/29 22:00
 */
class FlipsRepeatedImage extends PreHandleTask("反向复读图片") {
  /**
   * 执行前置任务
   *
   * @param event 消息对象（注意此时还没开始进行 SSDL 解析）
   * @param exec  异步上下文
   * @return 如果返回 false，将打断后续的 SSDL 执行
   */
  override def execute(event: EventMessage)(implicit exec: ExecutionContext): Future[Boolean] = async {
    val message = await(JamContext.messagePool.last(event))
    val isRepeat = message.exists(record => QQImg.isPicSame(event.message, record.message))
    if (isRepeat) {
      await(JamContext.messagePool.recordAPlaceholder(event))
      Future {
        logger.debug("准备翻转图片并发送")
        MessageImageUtil.getAndFlipImageFromMessage(event).map(_.toString).foreach(event.respond)
        logger.debug("翻转图片任务完成")
      }
    }
    true
  }
}

object FlipsRepeatedImage {
   private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(FlipsRepeatedImage.getClass)
}
