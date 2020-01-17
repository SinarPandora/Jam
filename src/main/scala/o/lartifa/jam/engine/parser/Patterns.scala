package o.lartifa.jam.engine.parser

import scala.util.matching.Regex

/**
 * Author: sinar
 * 2020/1/2 23:44
 */
object Patterns {
  // 1
  /**
   * 基本模式匹配
   * 返回结果：id content
   */
  val basePattern: Regex = """^(-?[0-9]+)[：:](.+)""".r("id", "content")
  // 2
  // 3
  /**
   * 消息捕获器匹配
   * 返回结果：type keyword
   */
  val matcherPattern: Regex = """^(当|如果)(句中出现|句首出现|句末出现|内容为|匹配)\{(.+?)\}时?[,，](.+)""".r("ignored", "type", "keyword", "command")

  // 4
  object ConditionPattern {
    // 5
    val paramCondition: Regex = """\{变量(.+?)\}的值(等于|大于|小于|不大于|不小于|不等于)\{((变量)?.+?)\}""".r("name", "op", "value")
    val senderCondition: Regex = """发送者的\{(昵称|QQ号|年龄|性别)\}为\{((变量)?.+?)\}""".r("info", "value")
    val sessionCondition: Regex = """会话的\{(类型|QQ号|群号)\}为\{((变量)?.+?)\}""".r("info", "value")
  }

  object LogicStructurePattern {
    // 3
    val `if`: Regex = """^(如果|若|当)(.+?)时[，,](.+)""".r("ignored", "condition", "command")
    // 2
    val or: Regex = """([，,]或(.+)[，,])|(或(.+))""".r("command")
    val and: Regex = """([，,][且并](.+)[，,])|([且并](.+))""".r("command")
    // 1
    val `else`: Regex = """[，,]否则(.+)""".r("command")
    val loopPattern: Regex = """循环\{(.+?)\}([0-9]+)次""".r("command", "times")
  }

  // 5
  object CommandPattern {
    // no
    val frequencyPattern: Regex = """(总是|偶尔|很少|极少)(.+)""".r("frequency", "command")
    val randomNumber: Regex = """随机[(（]([0-9]+)-([0-9]*)[)）]""".r("down", "up")
    val messageSend: Regex = """(回复|发送|说)\{((变量)?.+?)\}""".r("type", "message")
    val paramDel: Regex = """将\{变量(.+?)\}删除""".r("name")
    val paramOpt: Regex = """将\{变量(.+?)\}(增加|减少|乘以|除以|取余|设置为)\{((随机)?(变量)?.+?)\}""".r("name", "opt", "value")
    val goto: Regex = """执行步骤([0-9]+)""".r("stepId")
    val oneByOne: Regex = """依次执行([0-9|]+)""".r("stepIds")
    val randomGoto: Regex = """随机从\{([0-9|]+?)\}中选择\{([0-9])\}个执行""".r("stepIds", "amount")
    val waiting: Regex = """等待([0-9]+)秒""".r("sec")
    val noting: String = "什么也不做"
  }

}
