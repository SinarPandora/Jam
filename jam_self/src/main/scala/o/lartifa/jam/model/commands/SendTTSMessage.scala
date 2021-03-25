package o.lartifa.jam.model.commands

import cc.moecraft.icq.sender.message.components.ComponentTTS
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * TTS（文本转语音）指令
 *
 * Author: sinar
 * 2020/12/27 02:03
 */
case class SendTTSMessage(message: RenderStrTemplate) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = Future {
    message.execute().foreach(message => reply(new ComponentTTS(message)))
  }
}
