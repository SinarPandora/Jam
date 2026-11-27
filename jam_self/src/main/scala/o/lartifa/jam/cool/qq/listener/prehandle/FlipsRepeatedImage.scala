package o.lartifa.jam.cool.qq.listener.prehandle

import o.lartifa.jam.onebot.domain.ChatMessage
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.plugins.filppic.MessageImageUtil
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * Author: sinar
 * 2020/8/29 22:00
 */
object FlipsRepeatedImage extends PreHandleTask("反向复读图片") {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(FlipsRepeatedImage.getClass)
  /**
   * 执行前置任务
   *
   * @param event 消息对象（注意此时还没开始进行 SSDL 解析）
   * @param exec  异步上下文
   * @return 如果返回 false，将打断后续的 SSDL 执行
   */
  override def execute(event: ChatMessage)(implicit exec: ExecutionContext): Future[Boolean] =
    JamContext.messagePool.isPictureRepeat(event).flatMap {
      case true =>
        JamContext.messagePool.recordAPlaceholder(event, "捕获并翻转复读的图片").map { _ =>
          Future {
            logger.debug("准备翻转图片并发送")
            MessageImageUtil.getAndFlipImageFromMessage(event).map(_.toString).foreach(event.respond(_))
            logger.debug("翻转图片任务完成")
          }
          true
        }
      case false => Future.successful(true)
    }
}
