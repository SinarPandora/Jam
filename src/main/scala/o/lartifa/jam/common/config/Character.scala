package o.lartifa.jam.common.config

import com.typesafe.config.{Config, ConfigFactory}

/**
 * 性格配置
 *
 * Author: sinar
 * 2020/9/1 19:52
 */
object Character {
  val config: Config = ConfigFactory.load().getConfig("character")

  object RandomAIReply {
    private val reply: Config = config.getConfig("random_ai")
    val replayWhen1: String = reply.getString("1")
    val replyWhen100: String = reply.getString("100")
    val replyFrom2to20: String = reply.getString("2-20")
    val replyFrom21to40: String = reply.getString("21-40")
    val replyFrom41to60: String = reply.getString("41-60")
    val replyFrom61to80: String = reply.getString("61-80")
    val replyFrom81to99: String = reply.getString("81-99")
  }
}
