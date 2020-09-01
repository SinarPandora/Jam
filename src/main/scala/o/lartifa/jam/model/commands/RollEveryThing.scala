package o.lartifa.jam.model.commands

import java.security.SecureRandom

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.RollEveryThing.Mode

import scala.async.Async.async
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import scala.util.matching.Regex

/**
 * 超级 Roll 点
 *
 * Author: sinar
 * 2020/8/30 02:40
 */
case class RollEveryThing(mode: Mode, random: Random) extends Command[Int] {

  lazy val TRPG_ROLL: Regex = """(\d+)\s*?d\s*?(\d+)""".r("times", "size")

  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Int] = {
    mode match {
      case RollEveryThing.TRPG => TRPGRoll()
      case RollEveryThing.Simple1To100 => simple1To100()
      case RollEveryThing.MakeADecision => makeDecision()
      case RollEveryThing.RandomAI => randomAIReply()
    }
  }

  /**
   * TRPG 模式
   *
   * @param context 指令上下文
   * @param exec    异步上下文
   * @return 随机结果
   */
  private def TRPGRoll()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Int] = async {
    val event = context.eventMessage
    val times -> size = TRPG_ROLL.findFirstMatchIn(event.getMessage)
      .map(it => it.group("times").toInt -> it.group("size").toInt)
      .getOrElse(1 -> 100)
    if (times <= 1) {
      val result = random.nextInt(size) + 1
      event.respond(s"$atSender 掷出${times}d$size，结果为：$result")
      result
    } else {
      val results = (1 to times).map(_ => random.nextInt(size) + 1)
      val sum = results.sum
      event.respond(s"$atSender 掷出${times}d$size，依次为：${results.mkString("，")}，合计：${sum}点")
      sum
    }
  }

  /**
   * 简单的抛硬币决定
   *
   * @param context 指令上下文
   * @param exec    异步上下文
   * @return 随机结果
   */
  private def makeDecision()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Int] = async {
    val result = random.nextInt(2)
    val ab = if (result == 1) "正面" else "反面"
    context.eventMessage.respond(s"$atSender 抛了个硬币，是$ab")
    result
  }

  /**
   * 最简单的 1 到 100 的随机
   *
   * @param context 指令上下文
   * @param exec    异步上下文
   * @return 随机结果
   */
  private def simple1To100()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Int] = async {
    val result = random.nextInt(100) + 1
    context.eventMessage.respond(s"$atSender 投掷D100，结果为：$result")
    result
  }

  /**
   * 用伪随机数聊天模式
   *
   * @param context 指令上下文
   * @param exec    异步上下文
   * @return 随机结果
   */
  private def randomAIReply()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Int] = async {
    val result = random.nextInt(100) + 1
    val message = result match {
      case 1 => "这 是 真 理"
      case 100 => "異議あり！！"
      // 2 - 20
      case i if i > 1 && i <= 20 => "有一说一，非常赞同"
      // 21 - 40
      case i if i >= 21 && i <= 40 => "我觉得可以"
      // 41 - 60
      case i if i >= 41 && i <= 60 => "一般.jpg"
      // 61 - 80
      case i if i >= 61 && i <= 80 => "不太行"
      // 81 - 99
      case i if i >= 81 && i < 100 => "反对"
      // 不可能的情况
      case _ => "我混乱了。。。"
    }
    context.eventMessage.respond(s"$atSender $message（$result）")
    result
  }
}

object RollEveryThing {

  sealed abstract class Mode(val name: String)

  case object TRPG extends Mode("跑团Roll点")

  case object Simple1To100 extends Mode("普通投掷")

  case object MakeADecision extends Mode("抛硬币决策")

  case object RandomAI extends Mode("伪随机聊天")

  private lazy val simpleRandom: Random = new Random(System.currentTimeMillis())
  private lazy val secureRandom: Random = new SecureRandom()

  def apply(mode: Mode): RollEveryThing = {
    val random = mode match {
      case RandomAI => simpleRandom
      case _ => secureRandom
    }
    new RollEveryThing(mode, random)
  }

}
