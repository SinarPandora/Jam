package o.lartifa.jam.engine.parser

import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.model.commands._

import scala.util.Try

/**
 * SSDL 指令解析器
 *
 * Author: sinar
 * 2020/1/3 22:55 
 */
object CommandParser extends Parser {

  import Patterns.CommandPattern

  /**
   * 解析指令
   *
   * @param string 待解析字符串
   * @return 指令对象（Optional）
   */
  def parseCommand(string: String): Option[Command[_]] = {
    parseExecuteCommand(string) match {
      case Some(executableCommand) =>
        Some(executableCommand)
      case None =>
        LazyList(parseMessageSend _, parseGoto _, parseParamOpt _, parseRandomGoto _, parseParamDel _, parseWaiting _, parseDoNoting _)
          .map(_.apply(string))
          .find(_.isDefined)
          .flatten
    }
  }

  /**
   * 解析频率模式
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  private def parseExecuteCommand(string: String): Option[ExecutableCommand] = {
    import ExecutableCommand.Constant
    CommandPattern.frequencyPattern.findFirstMatchIn(string).map(result => {
      val frequency = result.group("frequency") match {
        case Constant.ALWAYS => ExecutableCommand.ALWAYS
        case Constant.RARELY => ExecutableCommand.RARELY
        case Constant.SOMETIME => ExecutableCommand.SOMETIME
        case Constant.VERY_RARELY => ExecutableCommand.VERY_RARELY
      }
      ExecutableCommand(frequency, parseCommand(result.group("command")).getOrElse(throw ParseFailException("解析失败！没有找到指令内容")))
    })
  }

  /**
   * 解析发送消息指令
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  private def parseMessageSend(string: String): Option[SendMessage] = {
    import SendMessage.Constant
    CommandPattern.messageSend.findFirstMatchIn(string).map(result => {
      val `type` = result.group("type") match {
        case s if Constant.SEND_TEXT.contains(s) => SendMessage.SEND_TEXT
        case Constant.SEND_PIC => SendMessage.SEND_PIC
      }
      result.group("message") match {
        case variable if variable.startsWith("变量") =>
          SendMessage(`type`, variable.stripPrefix("变量"), isMessageAParam = true)
        case message =>
          SendMessage(`type`, message)
      }
    })
  }

  /**
   * 解析随机数指令
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  private def parseRandomNumber(string: String): Option[RandomNumber] = {
    CommandPattern.randomNumber.findFirstMatchIn(string).map(result => {
      val down = Try(result.group("down").toInt).getOrElse(throw ParseFailException("随机数下界必须为整数"))
      val up = result.group("up") match {
        case "" => 999999
        case other => Try(other.toInt).getOrElse(throw ParseFailException("随机数上界必须为整数"))
      }
      need(up > down, "随机数的上界必须大于下界：A-B，A 必须大于 B（或者不填 B）")
      RandomNumber(down, up)
    })
  }

  /**
   * 解析变量删除指令
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  private def parseParamDel(string: String): Option[ParamDel] = {
    CommandPattern.paramDel.findFirstMatchIn(string).map(result => {
      ParamDel(result.group("name"))
    })
  }

  /**
   * 解析变量操作
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  private def parseParamOpt(string: String): Option[ParamOpt] = {
    import ParamOpt.Constant
    CommandPattern.paramOpt.findFirstMatchIn(string).map(result => {
      val paramName = result.group("name")
      val opt = result.group("opt") match {
        case Constant.PLUS => ParamOpt.PLUS
        case Constant.MINUS => ParamOpt.MINUS
        case Constant.TIMES => ParamOpt.TIMES
        case Constant.DIVIDED => ParamOpt.DIVIDED
        case Constant.MOD => ParamOpt.MOD
        case Constant.SET => ParamOpt.SET
      }
      result.group("value") match {
        case value if value.startsWith("随机") =>
          ParamOpt(paramName, opt, randomNumber = parseRandomNumber(value.stripPrefix("随机")))
        case value if value.startsWith("变量") =>
          ParamOpt(paramName, opt, value = value.stripPrefix("变量"), isValueAParam = true)
        case value =>
          ParamOpt(paramName, opt, value)
      }
    })
  }

  /**
   * 解析跳转执行指令
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  private def parseGoto(string: String): Option[GoTo] = {
    CommandPattern.goto.findFirstMatchIn(string).map(result =>
      GoTo(
        Try(result.group("stepId").toLong).getOrElse(throw ParseFailException("步骤 ID 必须为整数"))
      )
    )
  }

  /**
   * 解析随机执行事件指令
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  private def parseRandomGoto(string: String): Option[RandomGoto] = {
    CommandPattern.randomGoto.findFirstMatchIn(string).map(result => {
      val stepIds = Try(
        result
          .group("stepIds")
          .split("\\|")
          .map(_.toLong).toList)
        .getOrElse(throw ParseFailException("解析随机执行事件指令时失败，请检查指令 ID 列表格式是否为：数字|数字..."))
      val amount = Try(result.group("amount").toInt).getOrElse(throw ParseFailException("请输入正确的执行事件数量"))
      RandomGoto(stepIds, amount)
    })
  }

  /**
   * 解析等待指令
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  private def parseWaiting(string: String): Option[Waiting] = {
    CommandPattern.waiting.findFirstMatchIn(string).map(result => {
      Waiting(Try(result.group("sec").toInt).getOrElse(throw ParseFailException("等待时间设置过大")))
    })
  }

  /**
   * 解析什么都不做指令
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  private def parseDoNoting(string: String): Option[DoNoting.type] =
    if (CommandPattern.noting == string) Some(DoNoting) else None
}
