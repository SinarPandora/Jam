package o.lartifa.jam.model.commands

import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.RollEveryThing.{Mode, config}

import java.security.SecureRandom
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

  lazy val TRPG_ROLL: Regex = """(?<times>\d+)\s*?d\s*?(?<size>\d+)""".r

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
    reply(s"$atSender 抛了个硬币，是$ab")
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
    reply(s"$atSender 投掷D100，结果为：$result")
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
      case 1 => config.replayWhen1
      case 100 => config.replyWhen100
      case i if i > 1 && i <= 20 => config.replyFrom2to20
      case i if i >= 21 && i <= 40 => config.replyFrom21to40
      case i if i >= 41 && i <= 60 => config.replyFrom41to60
      case i if i >= 61 && i <= 80 => config.replyFrom61to80
      case i if i >= 81 && i < 100 => config.replyFrom81to99
      // 不可能的情况
      case _ => "我混乱了。。。"
    }
    reply(s"$atSender $message（$result，随机结果，仅供参考）")
    result
  }
}

object RollEveryThing {

  sealed abstract class Mode(val name: String)

  case object TRPG extends Mode("跑团Roll点")

  case object Simple1To100 extends Mode("普通投掷")

  case object MakeADecision extends Mode("抛硬币决策")

  case object RandomAI extends Mode("伪随机聊天")

  private lazy val secureRandom: Random = new SecureRandom()

  private def config: JamConfig.RandomAIReply = JamConfig.config.randomAIReply

  def apply(mode: Mode): RollEveryThing = {
    val random = mode match {
      case RandomAI => Random
      case _ => secureRandom
    }
    new RollEveryThing(mode, random)
  }

}
