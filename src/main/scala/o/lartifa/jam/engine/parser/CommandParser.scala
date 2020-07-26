package o.lartifa.jam.engine.parser

import ammonite.ops.PipeableImplicit
import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.model.commands._
import o.lartifa.jam.plugins.picbot._
import o.lartifa.jam.pool.JamContext

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
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 指令对象（Optional）
   */
  def parseCommand(string: String)(implicit context: ParseEngineContext): Option[Command[_]] = {
    parseExecuteCommand(string) match {
      case Some(executableCommand) =>
        Some(executableCommand)
      case None =>
        LazyList(
          parseCatchParameters _,
          parseMessageSend _,
          parseGoto _,
          parseOneByOne _,
          parseParamOpt _,
          parseRandomNumber _,
          parseRandomGoto _,
          parseLoopGoto _,
          parseParamDel _,
          parseWaiting _,
          parseSetPicFetcherMode _,
          parseSetPicRating _,
          parseRunTaskNow _,
          // 包含类模式放在后边
          parseDoNoting _,
          parseGroupWholeBan _,
          parseGroupWholeUnBan _,
          parseFetchAndSendPic _,
        )
          .map(_.apply(string))
          .find(_.isDefined)
          .flatten
          .map(command =>
            parseThenSaveTo(string, command)
              .getOrElse(command)
          )
    }
  }

  /**
   * 解析频率模式
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseExecuteCommand(string: String)(implicit context: ParseEngineContext): Option[ExecutableCommand] = {
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
   * 解析捕获指令内容指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseCatchParameters(string: String)(implicit context: ParseEngineContext): Option[CatchParameters] = {
    CommandPattern.catchParameters.findFirstMatchIn(string).map(result => {
      val regex = Try(result.group("regex").r).getOrElse(throw ParseFailException("正则表达式不合法"))
      val names = result.group("names").split("[,，]").toSeq
      if (names.forall(_.isBlank)) {
        throw ParseFailException("变量名不能为空或者空格")
      }
      CatchParameters(regex, names.map(_.asTempVar).zipWithIndex)
    })
  }

  /**
   * 解析发送消息指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseMessageSend(string: String)(implicit context: ParseEngineContext): Option[SendMessage] = {
    import SendMessage.Constant
    CommandPattern.messageSend.findFirstMatchIn(string).map(result => {
      val `type` = result.group("type") match {
        case Constant.SEND_TEXT => SendMessage.SEND_TEXT
        case Constant.SEND_PIC => SendMessage.SEND_PIC
        case Constant.SEND_AUDIO => SendMessage.SEND_AUDIO
      }
      SendMessage(`type`, context.getTemplate(result.group("template")))
    })
  }

  /**
   * 解析随机数指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseRandomNumber(string: String)(implicit context: ParseEngineContext): Option[RandomNumber] = {
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
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseParamDel(string: String)(implicit context: ParseEngineContext): Option[VarDel] = {
    CommandPattern.paramDel.findFirstMatchIn(string).map(result => {
      VarDel(context.getVar(result.group("name")))
    })
  }

  /**
   * 解析变量操作
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseParamOpt(string: String)(implicit context: ParseEngineContext): Option[VarOpt] = {
    import VarOpt.Constant
    CommandPattern.paramOpt.findFirstMatchIn(string).map(result => {
      val varKey = context.getVar(result.group("name"))
      val opt = result.group("opt") match {
        case Constant.PLUS => VarOpt.PLUS
        case Constant.MINUS => VarOpt.MINUS
        case Constant.TIMES => VarOpt.TIMES
        case Constant.DIVIDED => VarOpt.DIVIDED
        case Constant.MOD => VarOpt.MOD
        case Constant.SET => VarOpt.SET
      }
      val template = context.getTemplate(result.group("template"))
      VarOpt(varKey, opt, template)
    })
  }

  /**
   * 解析跳转执行指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseGoto(string: String)(implicit context: ParseEngineContext): Option[GoTo] = {
    CommandPattern.goto.findFirstMatchIn(string).map(result =>
      GoTo(
        Try(result.group("stepId").toLong).getOrElse(throw ParseFailException("步骤 ID 必须为整数"))
      )
    )
  }

  /**
   * 解析依次执行指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseOneByOne(string: String)(implicit context: ParseEngineContext): Option[OneByOne] = {
    CommandPattern.oneByOne.findFirstMatchIn(string).map(result => {
      val stepIds = Try(
        result
          .group("stepIds")
          .split("[,，]")
          .map(_.toLong).toList)
        .getOrElse(throw ParseFailException("解析依次执行事件指令时失败，请检查指令 ID 列表格式是否为：数字，数字..."))
      OneByOne(stepIds)
    })
  }

  /**
   * 解析随机执行指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseRandomGoto(string: String)(implicit context: ParseEngineContext): Option[RandomGoto] = {
    CommandPattern.randomGoto.findFirstMatchIn(string).map(result => {
      val stepIds = Try(
        result
          .group("stepIds")
          .split("[,，]")
          .map(_.toLong).toList)
        .getOrElse(throw ParseFailException("解析随机执行指令时失败，请检查指令 ID 列表格式是否为：数字，数字..."))
      val amount = Try(result.group("amount").toInt).getOrElse(throw ParseFailException("请输入正确的执行事件数量"))
      RandomGoto(stepIds, amount)
    })
  }

  /**
   * 解析循环执行指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseLoopGoto(string: String)(implicit context: ParseEngineContext): Option[LoopGoto] = {
    import LoopGoto._
    CommandPattern.loopGoto.findFirstMatchIn(string).map(result => {
      val stepIds = Try(
        result
          .group("stepIds")
          .split("[,，]")
          .map(_.toLong).toList)
        .getOrElse(throw ParseFailException("解析循环执行执行指令时失败，请检查指令 ID 列表格式是否为：数字，数字..."))
      val inOrder = result.group("inOrder") match {
        case InOrder.str => InOrder.value
        case NotInOrder.str => NotInOrder.value
      }
      val times = Try(result.group("amount").toInt).getOrElse(throw ParseFailException("请输入正确的执行事件数量"))
      LoopGoto(stepIds, inOrder, times)
    })
  }

  /**
   * 解析等待指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseWaiting(string: String)(implicit context: ParseEngineContext): Option[Waiting] = {
    CommandPattern.waiting.findFirstMatchIn(string).map(result => {
      Waiting(Try(result.group("sec").toInt).getOrElse(throw ParseFailException("等待时间设置过大")))
    })
  }

  /**
   * 解析什么都不做指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseDoNoting(string: String)(implicit context: ParseEngineContext): Option[DoNoting.type] =
    if (string.contains(CommandPattern.noting)) Some(DoNoting) else None

  /**
   * 解析全体禁言指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseGroupWholeBan(string: String)(implicit context: ParseEngineContext): Option[GroupWholeBan] =
    if (string.contains(CommandPattern.groupWholeBan)) Some(GroupWholeBan(true)) else None

  /**
   * 解析解除全体禁言指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseGroupWholeUnBan(string: String)(implicit context: ParseEngineContext): Option[GroupWholeBan] =
    if (string.contains(CommandPattern.groupWholeUnBan)) Some(GroupWholeBan(false)) else None

  /**
   * 解析发送色图指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseFetchAndSendPic(string: String)(implicit context: ParseEngineContext): Option[FetchAndSendPic] = {
    CommandPattern.fetchAndSendPic.findFirstMatchIn(string).map(result => {
      val amount = Try(result.group("amount").toInt).getOrElse(throw ParseFailException("张数格式不正确，请使用阿拉伯数字"))
      FetchAndSendPic(amount)
    })
  }

  /**
   * 解析设置图片获取模式指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseSetPicFetcherMode(string: String)(implicit context: ParseEngineContext): Option[SetPicFetcherMode] = {
    CommandPattern.setPicFetcherMode.findFirstMatchIn(string).map(result => {
      val mode = result.group("mode") match {
        case PatternMode.ONLY => ONLY
        case PatternMode.RANGE => RANGE
      }
      SetPicFetcherMode(mode)
    })
  }

  /**
   * 解析设置图片获取模式指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseSetPicRating(string: String)(implicit context: ParseEngineContext): Option[SetPicRating] = {
    CommandPattern.setPicRating.findFirstMatchIn(string).map(result => {
      val enableR18 = result.group("enableR18") match {
        case "允许" => true
        case "禁止" => false
        case other => throw ParseFailException(s"设置项不正确：$other")
      }
      SetPicRating(enableR18)
    })
  }

  /**
   * 解析保存结果指令
   *
   * @param string  待解析字符串
   * @param command 目标指令
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseThenSaveTo(string: String, command: Command[_])(implicit context: ParseEngineContext): Option[ThenSaveTo] = {
    Patterns.thenSaveTo.findFirstMatchIn(string).map(result => {
      val varKey = result.group("name") |> context.getVar
      ThenSaveTo(command, varKey)
    })
  }

  /**
   * 解析立即运行任务指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseRunTaskNow(string: String)(implicit context: ParseEngineContext): Option[RunTaskNow] = {
    CommandPattern.runTaskNow.findFirstMatchIn(string).map(result => {
      val name = result.group("task")
      JamContext.cronTaskPool.get().taskDefinition
        .getOrElse(name, throw ParseFailException(s"没有名为：${name}的定时任务"))
        .cls.getDeclaredConstructor()
        .newInstance() |> RunTaskNow.apply
    })
  }

}
