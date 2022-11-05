package o.lartifa.jam.engine.ssdl.parser

import scala.util.matching.Regex

/**
 * 全部 SSDL 正则表达式
 *
 * FIXME
 *  1. 因为变量会被预处理替换掉，所以下面出现的一切变量和模板，都应该只捕获大括号里面的内容
 *     2. 因为预处理的缘故，下文中出现的数组应该换用中括号【】和 []
 *
 * Author: sinar
 * 2020/1/2 23:44
 */
object Patterns {
  // 0
  /**
   * 变量匹配
   * 返回结果：type name
   */
  val varKeyPattern: Regex = """\{(?<type>临时变量|\*变量|变量|\*[@#&P图$]|[@#&P图$])(?<name>.+?)(?<default>\|.+?)?}""".r
  /**
   * 字符串模板匹配
   * 返回结果：template
   */
  val stringTemplatePattern: Regex = """(?s)%\{(?<template>.+?)}%""".r
  // 1
  /**
   * 基本模式匹配
   * 返回结果：id content
   */
  val basePattern: Regex = """^(?<id>auto|@|[0-9]+)[：:](?<content>.+)""".r
  // 2
  /**
   * TODO 立即执行！
   * 返回结果：command
   */
  val immediately: Regex = """^(?<stage>解析后|启动后)立刻(?<command>.+)""".r
  // 3
  /**
   * 消息捕获器匹配
   * 返回结果：type keyword command
   */
  val matcherPattern: Regex = """^(当|如果)(?<type>句中出现|句首出现|句末出现|内容为|匹配)%\{(?<template>.+?)}%时?[,，](?<command>.+)""".r
  val commandMatcherPattern: Regex = """注册前缀为[\[【](?<prefixes>[^{}【】\[\]]+)[】\]]的指令%\{(?<template>.+?)}%[:：](?<command>.+)""".r
  val eventMatcherPattern: Regex = """当接收到(?<event>.+?)事件时?[,，](?<command>.+)""".r

  // 4 -> STDL 解析

  // 平行 5
  object ConditionPattern {
    // 6
    val paramCondition: Regex = """\{(?<var>.+?)}的值(?<op>等于|大于|小于|不大于|不小于|不等于)%\{(?<template>.+?)}%""".r
  }

  // 平行 5
  object LogicStructurePattern {
    // 3
    val `if`: Regex = """^(如果|若|当)(?<condition>.+?)时[，,](?<command>.+)""".r
    // 2
    val or: Regex = new Regex("""([，,]或([^或]+)[，,]?)|(或([^或]+))""", "command")
    val and: Regex = new Regex("""([，,][且并]([^且并]+)[，,]?)|([且并]([^且并]+))""", "command")
    // 1
    val `else`: Regex = """[，,]否则(?<command>.+)""".r
    val loopPattern: Regex = """循环\{(?<command>.+?)}(?<times>[0-9]+)次""".r
  }

  // 6
  /**
   * 保存解析到的指令的执行结果
   */
  val thenSaveTo: Regex = """(?<command>.+?)之后将结果保存到\{(?<name>.+?)}""".r
  /**
   * 忽略错误
   */
  val ignoreError: Regex = """(?<command>.+?)忽略错误""".r

  // 7
  object CommandPattern {
    // no
    // 频率
    val frequencyPattern: Regex = """(?<frequency>总是|偶尔|很少|极少)(?<command>.+)""".r
    // 随机
    val randomNumber: Regex = """随机[(（](?<down>[0-9]+)-(?<up>[0-9]*)[)）]""".r
    // 捕获参数
    val catchParameters: Regex = """用\{(?<regex>.+?)}匹配捕获内容之后注册临时变量[\[【](?<names>[0-9A-Za-z一-龥,，]+)[】\]]""".r
    // 消息发送
    val messageSend: Regex = """(?<type>回复|发送|说)%\{(?<template>.+?)}%""".r
    // 变量删除
    val paramDel: Regex = """将\{(?<name>.+?)}删除""".r
    // 变量操作
    val paramOpt: Regex = """将\{(?<name>.+?)}(?<opt>增加|减少|乘以|除以|取余|设置为)%\{(?<template>.+?)}%""".r
    // 跳转执行
    val goto: Regex = """(执行步骤|跳转至)(?<stepId>[0-9]+)""".r
    // 依次执行
    val oneByOne: Regex = """依次执行[\[【](?<stepIds>[0-9,，]+)[】\]]""".r
    // 随机执行
    val randomGoto: Regex = """随机从[\[【](?<stepIds>[0-9,，]+)[】\]]中选择(?<amount>[0-9]+)个执行""".r
    // 循环执行
    val loopGoto: Regex = """(?<inOrder>循环顺序|循环)执行[\[【](?<stepIds>[0-9,，]+)[】\]](?<times>[0-9]+)次""".r
    // 等待
    val waiting: Regex = """等待(?<sec>[0-9]+)秒""".r
    // 什么也不做
    val noting: String = "什么也不做"
    // 全体禁言
    val groupWholeBan: String = "全体禁言"
    // 解除全体禁言
    val groupWholeUnBan: String = "解除全体禁言"
    // 发送"色图"
    val fetchAndSendPic: Regex = """发送(?<amount>[0-9]+)张色图""".r
    // 设置"色图"等级
    val setPicRating: Regex = """设置图片(?<enableR18>允许|禁止)R18""".r
    // 设置"色图"模式
    val setPicFetcherMode: Regex = """设置图片模式为(?<mode>仅当前|范围内)""".r
    // 获取图片信息
    val showPicInfo: String = "显示上一张图片的信息"
    // 高级随机
    val rollEveryThing: Regex = """(?<mode>跑团Roll点|普通投掷|抛硬币决策|伪随机聊天)""".r
    // TODO 注册定时任务
    val registerCronTask: Regex = """注册定时任务\{(?<stepId>[0-9]+)}为\{变量(?<value>.+?)}""".r
    // TODO 取消定时任务
    val deregisterCronTask: Regex = """取消定时任务\{变量(?<value>.+?)}""".r
    // 立即执行任务
    val runTaskNow: Regex = """立即执行任务\{(?<task>[0-9A-Za-z一-龥,，]+)}""".r
    // 询问
    val ask: Regex = """询问(?<questioner>发送者|任何人)[:：]%\{(?<question>.+?)}%[，,](?<answerMatchers>(若答案为\{.+?}|其他答案)[，,]则(.+)[;；]?)+""".r
    // 答案匹配器
    val answerMatcher: Regex = """(?<answer>若答案为\{.+?}|其他答案)[，,]则(?<command>[^;；]+)[;；]?""".r
    // 禁言某人
    val banSomeOneInGroup: Regex = """禁言%\{(?<qId>.+?)}%时长%\{(?<time>.+?)}%(?<unit>分钟|小时|天)""".r
    // 展示果酱可以做什么
    val whatICanDo: String = "展示一条可用的触发词"
    // QQ 骰子
    val qqDice: String = "发送QQ骰子"
    // QQ 猜拳
    val qqRPS: String = "发送石头剪刀布"
    // 抖一抖
    val shake: String = "发送抖一抖"
    // 发送视频
    val sendVideo: Regex = """发送视频%\{(?<file>.+?)}%""".r
    // 分享聊天
    val shareContact: Regex = """分享(?<type>群聊|好友)%\{(?<qId>.+?)}%""".r
    // 分享地理位置
    val shareLocation: Regex =
      """分享地理位置[：:]经度%\{(?<lat>.+?)}%纬度%\{(?<lon>.+?)}%标题%\{(?<title>.+?)}%内容%\{(?<content>.+?)}%""".r
    // 分享链接
    val shareURL: Regex =
      """分享链接[：:]地址%\{(?<url>.+?)}%标题%\{(?<title>.+?)}%内容%\{(?<content>.+?)}%图片%\{(?<image>.+?)}%""".r
    // 分享音乐
    val shareMusic: Regex = """分享(?<type>网易|QQ|虾米)音乐%\{(?<mId>.+?)}%""".r
    // 真·戳一戳指令
    val poke: Regex = """戳%\{(?<qId>.+?)}%""".r
    // TTS（文本转语音）指令
    val tts: Regex = """朗读%\{(?<message>.+?)}%""".r
    // 打断指令
    val breakDirectly: String = "立刻打断"
    // 视为未捕获指令
    val breakAsUnMatched: String = "打断视为未捕获"
    // 列出可用模型指令
    val listAIModels: String = "列出可用模型"
    // 坠梦指令
    val dropInDream: Regex = """(坠入梦境|联想回复)%\{(?<dream>.+?)}%""".r
    // 运行 Lambda 指令
    val runLambda: Regex = """([Ll]ambda|λ|=>)<(?<scriptPath>.+?)>(?<args>[(（].+?[)）])?""".r
    // Lambda 参数
    val lambdaArgs: Regex = """,?%\{(?<template>.+?)}%""".r
    // 配置源订阅
    val configSourcePush: String = "配置订阅源"
  }

}
