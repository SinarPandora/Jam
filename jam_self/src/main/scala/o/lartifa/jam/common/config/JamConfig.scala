package o.lartifa.jam.common.config

import com.typesafe.config.Config
import o.lartifa.jam.common.config.JamConfig.{Biochronometer, ForMaster, RandomAIReply}
import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.common.util.BetterConfig.*

import scala.jdk.CollectionConverters.*

/**
 * 性格配置
 *
 * Author: sinar
 * 2020/9/1 19:52
 */
case class JamConfig
(
  name: String,
  responseFrequency: Int,
  balderdash: List[String],
  forMaster: ForMaster,
  randomAIReply: RandomAIReply,
  biochronometer: Biochronometer,
  autoAcceptFriendRequest: Boolean,
  autoAcceptGroupRequest: Boolean,
  matchOutOfOrder: Boolean
)
object JamConfig extends Reloadable {
  case class ForMaster(masterList: List[Long], name: String, goodMorning: String, goodNight: String)

  case class RandomAIReply
  (
    replayWhen1: String,
    replyWhen100: String,
    replyFrom2to20: String,
    replyFrom21to40: String,
    replyFrom41to60: String,
    replyFrom61to80: String,
    replyFrom81to99: String,
  )

  case class Biochronometer
  (
    wakeUpTime: Int,
    goAsleepTime: Int,
    activeTimes: List[String],
    allTimeAtYourService: Boolean,
  )

  private var _config: Option[JamConfig] = None

  /**
   * 获取配置
   *
   * @return 配置对象
   */
  def config: JamConfig = this._config.getOrElse(throw ParseFailException("在配置尚未初始化前获取了其内容，是 BUG，请上报"))


  /**
   * 重新加载
   */
  override def reload(): Unit = {
    val config: Config = DynamicConfigLoader.config

    this._config = Option(new JamConfig(
      name = config.getStringOrElse("character.name", "果酱"),
      responseFrequency = config.getIntOrElse("character.response_frequency", 100),
      balderdash = config.getStringListOrElse("character.balderdash", List("睡着了……")),
      forMaster = ForMaster(
        masterList = config.getLongList("character.for_master.master_list").asScala.map(_.toLong).toList,
        name = config.getStringOrElse("character.for_master.name", "主人"),
        goodMorning = config.getStringOrElse("character.for_master.good_morning", "早上好！%s！"),
        goodNight = config.getStringOrElse("character.for_master.good_night", "晚安~ %s")
      ),
      randomAIReply = RandomAIReply(
        replayWhen1 = config.getStringOrElse("character.random_ai.1", "未配置"),
        replyWhen100 = config.getStringOrElse("character.random_ai.100", "未配置"),
        replyFrom2to20 = config.getStringOrElse("character.random_ai.2-20", "未配置"),
        replyFrom21to40 = config.getStringOrElse("character.random_ai.21-40", "未配置"),
        replyFrom41to60 = config.getStringOrElse("character.random_ai.41-60", "未配置"),
        replyFrom61to80 = config.getStringOrElse("character.random_ai.61-80", "未配置"),
        replyFrom81to99 = config.getStringOrElse("character.random_ai.81-99", "未配置"),
      ),
      biochronometer = Biochronometer(
        wakeUpTime = config.getIntOrElse("biochronometer.wake_up_time", 8),
        goAsleepTime = config.getIntOrElse("biochronometer.go_asleep_time", 1),
        activeTimes = config.getStringListOrElse("biochronometer.active_times", List("None")),
        allTimeAtYourService = config.getBooleanOrElse("biochronometer.all_time_at_your_service", default = false)
      ),
      autoAcceptFriendRequest = config.getBooleanOrElse("character.auto_accept_friend_request", default = true),
      autoAcceptGroupRequest = config.getBooleanOrElse("character.auto_accept_group_request", default = true),
      matchOutOfOrder = config.getBooleanOrElse("match_out_of_order", default = false)
    ))
  }
}
