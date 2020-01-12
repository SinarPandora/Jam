package o.lartifa.jam.model.commands

import java.util.Base64

import better.files.File
import cc.moecraft.icq.sender.message.MessageBuilder
import cc.moecraft.icq.sender.message.components.{ComponentImage, ComponentImageBase64}
import cc.moecraft.icq.sender.returndata.ReturnData
import cc.moecraft.icq.sender.returndata.returnpojo.send.RMessageReturnData
import o.lartifa.jam.common.exception.{ExecuteException, ParamNotFoundException}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.SendMessage.Type
import o.lartifa.jam.pool.JamContext

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * 指令：发送回复消息
 *
 * Author: sinar
 * 2020/1/3 23:05 
 */
case class SendMessage(`type`: Type, message: String, isMessageAParam: Boolean = false) extends Command[ReturnData[RMessageReturnData]] {

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
    val message = await(getMessageContent())
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
    val message = await(getMessageContent())
    val content = new MessageBuilder().add({
      if (message.toLowerCase.startsWith("http://") || message.toLowerCase.startsWith("http://")) {
        new ComponentImage(message)
      } else {
        val bytes = Try(File(message).byteArray).getOrElse(throw ExecuteException(s"图片文件获取失败，地址：$message"))
        new ComponentImageBase64(Base64.getEncoder.encodeToString(bytes))
      }
    }).toString
    Try(context.eventMessage.respond(content)) match {
      case Failure(exception) =>
        exception.printStackTrace()
        throw ExecuteException("图片发送可能失败，请检查是否图片较大或地址不正确")
      case Success(value) => value
    }
  }

  /**
   * 获取消息真实内容
   *
   * @param context 指令执行上下文
   * @param exec    异步执行上下午
   * @return 真实内容
   */
  def getMessageContent()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[String] = async {
    if (isMessageAParam) {
      await(JamContext.variablePool.get(message)).getOrElse(throw ParamNotFoundException(message))
    } else message
  }
}

object SendMessage {

  sealed class Type

  case object SEND_TEXT extends Type

  case object SEND_PIC extends Type

  object Constant {
    val SEND_TEXT: List[String] = List("回复", "说")
    val SEND_PIC: String = "发送"

    object MessageType {
      val PRIVATE = "private"
      val GROUP = "group"
      val DISCUSS = "discuss"
    }

  }

}
