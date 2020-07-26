package o.lartifa.jam.model.commands

import java.util.Base64

import better.files.File
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.{ComponentImage, ComponentImageBase64, ComponentRecord}
import cc.moecraft.icq.sender.returndata.ReturnData
import cc.moecraft.icq.sender.returndata.returnpojo.send.RMessageReturnData
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.SendMessage.Type

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * 指令：发送回复消息
 *
 * Author: sinar
 * 2020/1/3 23:05
 */
case class SendMessage(`type`: Type, template: RenderStrTemplate) extends Command[ReturnData[RMessageReturnData]] {

  /**
   * 发送回复消息
   *
   * @param context 指令执行上下文
   * @param exec    异步执行上下文
   * @return 异步结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[ReturnData[RMessageReturnData]] = {
    (`type`: @unchecked) match {
      case SendMessage.SEND_TEXT => sendTextMessage()
      case SendMessage.SEND_PIC => sendPic()
      case SendMessage.SEND_AUDIO => sendPic()
    }
  }

  /**
   * 发送文字信息
   *
   * @param context 指令执行上下文
   * @param exec    异步执行上下文
   * @return 异步消息返回对象
   */
  def sendTextMessage()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[ReturnData[RMessageReturnData]] = async {
    val message = await(template.execute())
    context.eventMessage.respond(message)
  }

  /**
   * 发送图片消息
   *
   * @param context 指令执行上下文
   * @param exec    异步执行上下文
   * @return 异步消息返回对象
   */
  def sendPic()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[ReturnData[RMessageReturnData]] = async {
    val message = await(template.execute())
    val content = new MessageBuilder().add({
      val lowCaseName = message.toLowerCase
      if (lowCaseName.startsWith("http://") || lowCaseName.startsWith("https://") || lowCaseName.contains("data/image") || lowCaseName.contains("data\\image")) {
        new ComponentImage(message)
      } else {
        val bytes = Try(File(message).byteArray).getOrElse(throw ExecutionException(s"图片文件获取失败，地址：$message"))
        new ComponentImageBase64(Base64.getEncoder.encodeToString(bytes))
      }
    }).toString
    Try(context.eventMessage.respond(content)) match {
      case Failure(exception) =>
        throw ExecutionException("图片发送可能失败，请检查是否图片较大或地址不正确").initCause(exception)
      case Success(value) => value
    }
  }

  /**
   * 发送语音消息
   * 由于酷Q系统限制，语音文件必须位于酷Q文件夹/data/record 下
   *
   * @param context 执行上下文
   * @param exec    异步执行上下文
   * @return 异步消息返回对象
   */
  def sendAudio()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[ReturnData[RMessageReturnData]] = async {
    val audioUri = await(template.execute())
    val audioMessage = new MessageBuilder().add(new ComponentRecord(audioUri)).toString
    Try(context.eventMessage.respond(audioMessage)) match {
      case Failure(exception) =>
        throw ExecutionException("图片发送可能失败，请检查是否语音文件较大或语音文件没有存放在酷Q /data/record 目录下").initCause(exception)
      case Success(value) => value
    }
  }
}

object SendMessage {

  sealed class Type

  case object SEND_TEXT extends Type

  case object SEND_PIC extends Type

  case object SEND_AUDIO extends Type

  object Constant {
    val SEND_TEXT: String = "回复"
    val SEND_PIC: String = "发送"
    val SEND_AUDIO: String = "说"
  }

}
