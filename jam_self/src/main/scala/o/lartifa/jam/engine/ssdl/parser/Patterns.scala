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
  val varKeyPattern: Regex = """\{(临时变量|\*变量|变量|\*[@#&P图$]|[@#&P图$])(.+?)(\|.+?)?}""".r("type", "name", "default")
  /**
   * 字符串模板匹配
   * 返回结果：template
   */
  val stringTemplatePattern: Regex = """(?s)%\{(.+?)}%""".r("template")
  // 1
  /**
   * 基本模式匹配
   * 返回结果：id content
   */
  val basePattern: Regex = """^(-?[0-9]+)[：:](.+)""".r("id", "content")
  // 2
  /**
   * TODO 立即执行！
   * 返回结果：command
   */
  val immediately: Regex = """^(解析后|启动后)立刻(.+)""".r("ignored", "command")
  // 3
  /**
   * 消息捕获器匹配
   * 返回结果：type keyword command
   */
  val matcherPattern: Regex = """^(当|如果)(句中出现|句首出现|句末出现|内容为|匹配)%\{(.+?)}%时?[,，](.+)""".r("ignored", "type", "template", "command")

  // 4 -> STDL 解析

  // 平行 5
  object ConditionPattern {
    // 6
    val paramCondition: Regex = """\{(.+?)}的值(等于|大于|小于|不大于|不小于|不等于)%\{(.+?)}%""".r("var", "op", "template")
  }

  // 平行 5
  object LogicStructurePattern {
    // 3
    val `if`: Regex = """^(如果|若|当)(.+?)时[，,](.+)""".r("ignored", "condition", "command")
    // 2
    val or: Regex = """([，,]或([^或]+)[，,]?)|(或([^或]+))""".r("command")
    val and: Regex = """([，,][且并]([^且并]+)[，,]?)|([且并]([^且并]+))""".r("command")
    // 1
    val `else`: Regex = """[，,]否则(.+)""".r("command")
    val loopPattern: Regex = """循环\{(.+?)}([0-9]+)次""".r("command", "times")
  }

  // 6
  /**
   * 保存解析到的指令的执行结果
   */
  val thenSaveTo: Regex = """(.+?)之后将结果保存到\{(.+?)}""".r("command", "name")
  /**
   * 忽略错误
   */
  val ignoreError: Regex = """(.+?)忽略错误""".r("command")

  // 7
  object CommandPattern {
    // no
    // 频率
    val frequencyPattern: Regex = """(总是|偶尔|很少|极少)(.+)""".r("frequency", "command")
    // 随机
    val randomNumber: Regex = """随机[(（]([0-9]+)-([0-9]*)[)）]""".r("down", "up")
    // 捕获参数
    val catchParameters: Regex = """用\{(.+?)}匹配捕获内容之后注册临时变量[\[【]([0-9A-Za-z一-龥,，]+)[】\]]""".r("regex", "names")
    // 消息发送
    val messageSend: Regex = """(回复|发送|说)%\{(.+?)}%""".r("type", "template")
    // 变量删除
    val paramDel: Regex = """将\{(.+?)}删除""".r("name")
    // 变量操作
    val paramOpt: Regex = """将\{(.+?)}(增加|减少|乘以|除以|取余|设置为)%\{(.+?)}%""".r("name", "opt", "template")
    // 跳转执行
    val goto: Regex = """(执行步骤|跳转至)([0-9]+)""".r("ignored", "stepId")
    // 依次执行
    val oneByOne: Regex = """依次执行[\[【]([0-9,，]+)[】\]]""".r("stepIds")
    // 随机执行
    val randomGoto: Regex = """随机从[\[【]([0-9,，]+)[】\]]中选择([0-9]+)个执行""".r("stepIds", "amount")
    // 循环执行
    val loopGoto: Regex = """(循环顺序|循环)执行[\[【]([0-9,，]+)[】\]]([0-9]+)次""".r("inOrder", "stepIds", "times")
    // 等待
    val waiting: Regex = """等待([0-9]+)秒""".r("sec")
    // 什么也不做
    val noting: String = "什么也不做"
    // 全体禁言
    val groupWholeBan: String = "全体禁言"
    // 解除全体禁言
    val groupWholeUnBan: String = "解除全体禁言"
    // 发送"色图"
    val fetchAndSendPic: Regex = """发送([0-9]+)张色图""".r("amount")
    // 设置"色图"等级
    val setPicRating: Regex = """设置图片(允许|禁止)R18""".r("enableR18")
    // 设置"色图"模式
    val setPicFetcherMode: Regex = """设置图片模式为(仅当前|范围内)""".r("mode")
    // 获取图片信息
    val showPicInfo: String = "显示上一张图片的信息"
    // 高级随机
    val rollEveryThing: Regex = """(跑团Roll点|普通投掷|抛硬币决策|伪随机聊天)""".r("mode")
    // TODO 注册定时任务
    val registerCronTask: Regex = """注册定时任务\{([0-9]+)}为\{变量(.+?)}""".r("stepId", "value")
    // TODO 取消定时任务
    val deregisterCronTask: Regex = """取消定时任务\{变量(.+?)}""".r("value")
    // 立即执行任务
    val runTaskNow: Regex = """立即执行任务\{([0-9A-Za-z一-龥,，]+)}""".r("task")
    // 源订阅
    val rssSubscribe: String = "订阅消息中的源"
    // 源退订
    val rssUnSubscribe: String = "退订消息中的源"
    // 源列出
    val rssShowAll: String = "列出当前会话订阅的源"
    // 询问
    val ask: Regex = """询问(发送者|任何人)[:：]%\{(.+?)}%[，,]((若答案为\{.+?}|其他答案)则(.+)[;；]?)+""".r("questioner", "question", "answerMatchers")
    // 答案匹配器
    val answerMatcher: Regex = """(若答案为\{.+?}|其他答案)则([^;；]+)[;；]?""".r("answer", "command")
    // 禁言某人
    val banSomeOneInGroup: Regex = """禁言%\{(.+?)}%时长%\{(.+?)}%(分钟|小时|天)""".r("qId", "time", "unit")
    // 展示果酱可以做什么
    val whatICanDo: String = "展示一条可用的触发词"
    // QQ 骰子
    val qqDice: String = "发送QQ骰子"
    // QQ 猜拳
    val qqRPS: String = "发送石头剪刀布"
    // 抖一抖
    val shake: String = "发送抖一抖"
    // 发送视频
    val sendVideo: Regex = """发送视频%\{(.+?)}%""".r("file")
    // 分享聊天
    val shareContact: Regex = """分享(群聊|好友)%\{(.+?)}%""".r("type", "qId")
    // 分享地理位置
    val shareLocation: Regex =
      """分享地理位置[：:]经度%\{(.+?)}%纬度%\{(.+?)}%标题%\{(.+?)}%内容%\{(.+?)}%"""
        .r("lat", "lon", "title", "content")
    // 分享链接
    val shareURL: Regex =
      """分享链接[：:]地址%\{(.+?)}%标题%\{(.+?)}%内容%\{(.+?)}%图片%\{(.+?)}%"""
        .r("url", "title", "content", "image")
    // 分享音乐
    val shareMusic: Regex = """分享(网易|QQ|虾米)音乐%\{(.+?)}%""".r("type", "mId")
    // 彩云小梦模式
    val startDreaming: String = "在当前会话开启坠梦模式"
    // 链接到指定梦境
    val linkToDream: String = "链接到消息中的梦境"
    // 真·戳一戳指令
    val poke: Regex = """戳%\{(.+?)}%""".r("qId")
    // TTS（文本转语音）指令
    val tts: Regex = """朗读%\{(.+?)}%""".r("message")
  }

}
