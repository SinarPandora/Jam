package o.lartifa.jam.engine.ssdl.parser

import ammonite.ops.PipeableImplicit
import cc.moecraft.icq.sender.message.components.ComponentContact.ContactType
import cc.moecraft.icq.sender.message.components.ComponentMusic.MusicSourceType
import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.engine.proto.Parser
import o.lartifa.jam.model.commands.*
import o.lartifa.jam.model.commands.Ask.{AnyBody, CurrentSender}
import o.lartifa.jam.plugins.JamPluginLoader
import o.lartifa.jam.plugins.picbot.*
import o.lartifa.jam.plugins.rss.{RSSShowAll, RSSSubscribe, RSSUnSubscribe}
import o.lartifa.jam.pool.JamContext

import java.util.concurrent.TimeUnit
import scala.collection.parallel.CollectionConverters.*
import scala.util.Try

/**
 * SSDL 指令解析器
 *
 * Author: sinar
 * 2020/1/3 22:55
 */
object CommandParser extends Parser {

  type LineParser = (String, ParseEngineContext) => Option[Command[?]]

  import Patterns.CommandPattern

  private var _parsers: List[LineParser] = Nil

  /**
   * 准备指令解析器
   */
  def prepareParsers(): Unit = {
    _parsers = createParserList
  }

  /**
   * 获构建指令解析器列表
   *
   * @return 解析器列表
   */
  private def createParserList: List[LineParser] = {
    val components = JamPluginLoader.loadedComponents
    val contains = components.containsModeCommandParsers.map(it => it.parse _)
    val regex = components.regexModeCommandParsers.map(it => it.parse _)
    val highOrder = components.highOrderModeCommandParsers.map(it => it.parse _)
    List(parseAsk _) ++ highOrder ++ List(
      parseCatchParameters _, parseMessageSend _, parseGoto _, parseOneByOne _, parseParamOpt _,
      parseRandomNumber _, parseRandomGoto _, parseLoopGoto _, parseParamDel _, parseWaiting _,
      parseSetPicFetcherMode _, parseSetPicRating _, parseRunTaskNow _, parseFetchAndSendPic _,
      parseRollEveryThing _, parseBanSomeOneInGroup _, parseSendVideo _, parseShareLocation _,
      parseShareURL _, parseShareContact _, parseShareMusic _, parsePoke _, parseTTS _) ++ regex ++ List(
      // 包含类模式放在后边
      parseDoNoting _, parseGroupWholeBan _, parseGroupWholeUnBan _, parseShowPicInfo _,
      parseRSSSubscribe _, parseRSSUnSubscribe _, parseRSSShowAll _, parseWhatICanDo _,
      parseQQDice _, parseQQRPS _, parseShake _, parseBreakDirectly _, parseBreakAsUnMatched _, parseListAIModels _
    ) ++ contains
  }

  /**
   * 解析指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 指令对象（Optional）
   */
  def parseCommand(string: String, context: ParseEngineContext): Option[Command[?]] = {
    parseExecuteCommand(string, context) match {
      case Some(executableCommand) =>
        Some(executableCommand)
      case None =>
        _parsers
          .map(_.apply(string, context))
          .find(_.isDefined)
          .flatten
          .map(command => parseIgnoreError(string, command).getOrElse(command))
          .map(command => parseThenSaveTo(string, command, context).getOrElse(command))
    }
  }

  /**
   * 解析频率模式
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseExecuteCommand(string: String, context: ParseEngineContext): Option[ExecutableCommand] = {
    import ExecutableCommand.Constant
    CommandPattern.frequencyPattern.findFirstMatchIn(string).map(result => {
      val frequency = result.group("frequency") match {
        case Constant.ALWAYS => ExecutableCommand.ALWAYS
        case Constant.RARELY => ExecutableCommand.RARELY
        case Constant.SOMETIME => ExecutableCommand.SOMETIME
        case Constant.VERY_RARELY => ExecutableCommand.VERY_RARELY
      }
      ExecutableCommand(frequency, parseCommand(result.group("command"), context)
        .getOrElse(throw ParseFailException("解析失败！没有找到指令内容")))
    })
  }

  /**
   * 解析捕获指令内容指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseCatchParameters(string: String, context: ParseEngineContext): Option[CatchParameters] = {
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
  private def parseMessageSend(string: String, context: ParseEngineContext): Option[SendMessage] = {
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
  private def parseRandomNumber(string: String, context: ParseEngineContext): Option[RandomNumber] = {
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
  private def parseParamDel(string: String, context: ParseEngineContext): Option[VarDel] = {
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
  private def parseParamOpt(string: String, context: ParseEngineContext): Option[VarOpt] = {
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
  private def parseGoto(string: String, context: ParseEngineContext): Option[GoTo] = {
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
  private def parseOneByOne(string: String, context: ParseEngineContext): Option[OneByOne] = {
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
  private def parseRandomGoto(string: String, context: ParseEngineContext): Option[RandomGoto] = {
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
  private def parseLoopGoto(string: String, context: ParseEngineContext): Option[LoopGoto] = {
    import LoopGoto.*
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
  private def parseWaiting(string: String, context: ParseEngineContext): Option[Waiting] = {
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
  private def parseDoNoting(string: String, context: ParseEngineContext): Option[DoNoting.type] =
    if (string.contains(CommandPattern.noting)) Some(DoNoting) else None

  /**
   * 解析全体禁言指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseGroupWholeBan(string: String, context: ParseEngineContext): Option[GroupWholeBan] =
    if (string.contains(CommandPattern.groupWholeBan)) Some(GroupWholeBan(true)) else None

  /**
   * 解析解除全体禁言指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseGroupWholeUnBan(string: String, context: ParseEngineContext): Option[GroupWholeBan] =
    if (string.contains(CommandPattern.groupWholeUnBan)) Some(GroupWholeBan(false)) else None

  /**
   * 解析发送色图指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseFetchAndSendPic(string: String, context: ParseEngineContext): Option[FetchAndSendPic] = {
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
  private def parseSetPicFetcherMode(string: String, context: ParseEngineContext): Option[SetPicFetcherMode] = {
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
  private def parseSetPicRating(string: String, context: ParseEngineContext): Option[SetPicRating] = {
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
   * 解析查看图片信息指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseShowPicInfo(string: String, context: ParseEngineContext): Option[ShowPicInfo] =
    if (string.contains(CommandPattern.showPicInfo)) Some(ShowPicInfo()) else None

  /**
   * 解析高级随机指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseRollEveryThing(string: String, context: ParseEngineContext): Option[RollEveryThing] = {
    CommandPattern.rollEveryThing.findFirstMatchIn(string).map(result => {
      val mode = result.group("mode") match {
        case RollEveryThing.TRPG.name => RollEveryThing.TRPG
        case RollEveryThing.Simple1To100.name => RollEveryThing.Simple1To100
        case RollEveryThing.MakeADecision.name => RollEveryThing.MakeADecision
        case RollEveryThing.RandomAI.name => RollEveryThing.RandomAI
        case other => throw ParseFailException(s"设置项不正确：$other")
      }
      RollEveryThing(mode)
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
  private def parseThenSaveTo(string: String, command: Command[?], context: ParseEngineContext): Option[ThenSaveTo] = {
    Patterns.thenSaveTo.findFirstMatchIn(string).map(result => {
      val varKey = result.group("name") |> context.getVar
      ThenSaveTo(command, varKey)
    })
  }

  /**
   * 解析忽略错误指令
   *
   * @param string  待解析字符串
   * @param command 目标指令
   * @return 解析结果
   */
  private def parseIgnoreError(string: String, command: Command[?]): Option[IgnoreError] =
    Patterns.ignoreError.findFirstMatchIn(string).map(_ => IgnoreError(command))

  /**
   * 解析立即运行任务指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseRunTaskNow(string: String, context: ParseEngineContext): Option[RunTaskNow] = {
    CommandPattern.runTaskNow.findFirstMatchIn(string).map(result => {
      val name = result.group("task")
      val pool = JamContext.cronTaskPool.get()
      val task = pool.getActiveTasks(name) match {
        case Left(singletonTask) => Left(singletonTask)
        case Right(_) => Right(pool.taskDefinition(name))
      }
      RunTaskNow(task)
    })
  }

  /**
   * 解析源订阅指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseRSSSubscribe(string: String, context: ParseEngineContext): Option[RSSSubscribe.type] =
    if (string.contains(CommandPattern.rssSubscribe)) Some(RSSSubscribe) else None

  /**
   * 解析源退订指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseRSSUnSubscribe(string: String, context: ParseEngineContext): Option[RSSUnSubscribe.type] =
    if (string.contains(CommandPattern.rssUnSubscribe)) Some(RSSUnSubscribe) else None

  /**
   * 解析列出当前会话订阅源指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseRSSShowAll(string: String, context: ParseEngineContext): Option[RSSShowAll.type] =
    if (string.contains(CommandPattern.rssShowAll)) Some(RSSShowAll) else None

  /**
   * 解析询问指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseAsk(string: String, context: ParseEngineContext): Option[Ask] = {
    CommandPattern.ask.findFirstMatchIn(string).map(result => {
      val answererType = result.group("questioner") match {
        case CurrentSender.name => CurrentSender
        case AnyBody.name => AnyBody
        case _ => throw ParseFailException("应答者类型有误，应在：发送者 和 任何人 中选择")
      }
      val matchers = CommandPattern.answerMatcher.findAllMatchIn(result.group("answerMatchers")).toList.par
        .map { it =>
          val command = parseCommand(it.group("command"), context).getOrElse(throw ParseFailException("答案对应的指令不正确"))
          val answer = it.group("answer")
          if (answer == "其他答案") None -> command
          else Some(answer.stripPrefix("若答案为{").stripSuffix("}")) -> command
        }.seq.toMap
      val defaultCallback = matchers.get(None)
      val answerMatcher: Map[String, Command[?]] = (matchers - None).map {
        case (Some(key), value) => key -> value
        case _ => throw ParseFailException("解析答案过程中出现错误")
      }
      Ask(context.getTemplate(result.group("question")), answererType, answerMatcher, defaultCallback)
    })
  }

  /**
   * 解析禁言某人指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseBanSomeOneInGroup(string: String, context: ParseEngineContext): Option[BanSomeOneInGroup] = {
    CommandPattern.banSomeOneInGroup.findFirstMatchIn(string).map(result => {
      val qId = result.group("qId") |> context.getTemplate
      val time = result.group("time") |> context.getTemplate
      val unit: TimeUnit = result.group("unit") match {
        case "分钟" => TimeUnit.MINUTES
        case "小时" => TimeUnit.HOURS
        case "天" => TimeUnit.DAYS
        case _ => throw ParseFailException("不支持的时间单位")
      }
      BanSomeOneInGroup(qId, time, unit)
    })
  }

  /**
   * 解析 "展示果酱可以做什么" 指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseWhatICanDo(string: String, context: ParseEngineContext): Option[WhatICanDo.type] =
    if (string.contains(CommandPattern.whatICanDo)) Some(WhatICanDo) else None

  /**
   * 解析发送 QQ 骰子指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseQQDice(string: String, context: ParseEngineContext): Option[QQDice.type] =
    if (string.contains(CommandPattern.qqDice)) Some(QQDice) else None

  /**
   * 解析发送 QQ 猜拳指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseQQRPS(string: String, context: ParseEngineContext): Option[QQRPS.type] =
    if (string.contains(CommandPattern.qqRPS)) Some(QQRPS) else None

  /**
   * 解析抖一抖指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseShake(string: String, context: ParseEngineContext): Option[Shake.type] =
    if (string.contains(CommandPattern.shake)) Some(Shake) else None

  /**
   * 解析发送视频指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseSendVideo(string: String, context: ParseEngineContext): Option[SendVideo] = {
    CommandPattern.sendVideo.findFirstMatchIn(string).map(result => {
      SendVideo(context.getTemplate(result.group("file")))
    })
  }

  /**
   * 解析分享位置指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseShareLocation(string: String, context: ParseEngineContext): Option[ShareLocation] = {
    CommandPattern.shareLocation.findFirstMatchIn(string).map(result => {
      import context.getTemplate
      import result.group
      ShareLocation(getTemplate(group("lat")), getTemplate(group("lon")),
        getTemplate(group("title")), getTemplate(group("content")))
    })
  }

  /**
   * 解析分享链接指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseShareURL(string: String, context: ParseEngineContext): Option[ShareURL] = {
    CommandPattern.shareURL.findFirstMatchIn(string).map(result => {
      import context.getTemplate
      import result.group
      ShareURL(getTemplate(group("url")), getTemplate(group("title")),
        getTemplate(group("content")), getTemplate(group("image")))
    })
  }

  /**
   * 解析分享聊天指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseShareContact(string: String, context: ParseEngineContext): Option[ShareContact] = {
    CommandPattern.shareContact.findFirstMatchIn(string).map(result => {
      val contactType = result.group("type") match {
        case ShareContact.Friend.str => ContactType.qq
        case ShareContact.Group.str => ContactType.group
      }
      ShareContact(context.getTemplate(result.group("qId")), contactType)
    })
  }

  /**
   * 解析分享音乐指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseShareMusic(string: String, context: ParseEngineContext): Option[ShareMusic] = {
    CommandPattern.shareMusic.findFirstMatchIn(string).map(result => {
      val sourceType = result.group("type") match {
        case ShareMusic.Netease.str => MusicSourceType.netease
        case ShareMusic.QQ.str => MusicSourceType.qq
        case ShareMusic.XM.str => MusicSourceType.xm
      }
      ShareMusic(context.getTemplate(result.group("mId")), sourceType)
    })
  }

  /**
   * 解析真·戳一戳指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parsePoke(string: String, context: ParseEngineContext): Option[Poke] = {
    CommandPattern.poke.findFirstMatchIn(string).map(result => {
      Poke(context.getTemplate(result.group("qId")))
    })
  }

  /**
   * 解析TTS（文本转语音）指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseTTS(string: String, context: ParseEngineContext): Option[SendTTSMessage] = {
    CommandPattern.tts.findFirstMatchIn(string).map(result => {
      SendTTSMessage(context.getTemplate(result.group("message")))
    })
  }

  /**
   * 解析打断指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseBreakDirectly(string: String, context: ParseEngineContext): Option[BreakDirectly.type] =
    if (string.contains(CommandPattern.breakDirectly)) Some(BreakDirectly) else None

  /**
   * 解析视为未捕获指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseBreakAsUnMatched(string: String, context: ParseEngineContext): Option[BreakAsUnMatched.type] =
    if (string.contains(CommandPattern.breakAsUnMatched)) Some(BreakAsUnMatched) else None

  /**
   * 解析列出可用模型指令
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseListAIModels(string: String, context: ParseEngineContext): Option[ListAIModels.type] =
    if (string.contains(CommandPattern.listAIModels)) Some(ListAIModels) else None
}
