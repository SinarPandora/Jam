package o.lartifa.jam.common.config

import com.typesafe.config.Config
import o.lartifa.jam.common.config.JamConfig.{Biochronometer, ForMaster, RandomAIReply}
import o.lartifa.jam.common.exception.ParseFailException

import scala.jdk.CollectionConverters.*

/**
 * 性格配置
 *
 * Author: sinar
 * 2020/9/1 19:52
 */
case class JamConfig
(
  balderdash: List[String],
  forMaster: ForMaster,
  randomAIReply: RandomAIReply,
  biochronometer: Biochronometer
)
object JamConfig extends Reloadable {
  case class ForMaster(name: String, goodMorning: String, goodNight: String)
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

    this._config = Some(
      new JamConfig(
        balderdash = config.getStringList("character.balderdash").asScala.toList,
        forMaster = ForMaster(
          name = config.getString("character.for_master.name"),
          goodMorning = config.getString("character.for_master.good_morning"),
          goodNight = config.getString("character.for_master.good_night")
        ),
        randomAIReply = RandomAIReply(
          replayWhen1 = config.getString("character.random_ai.1"),
          replyWhen100 = config.getString("character.random_ai.100"),
          replyFrom2to20 = config.getString("character.random_ai.2-20"),
          replyFrom21to40 = config.getString("character.random_ai.21-40"),
          replyFrom41to60 = config.getString("character.random_ai.41-60"),
          replyFrom61to80 = config.getString("character.random_ai.61-80"),
          replyFrom81to99 = config.getString("character.random_ai.81-99"),
        ),
        biochronometer = Biochronometer(
          wakeUpTime = config.getInt("biochronometer.wake_up_time"),
          goAsleepTime = config.getInt("biochronometer.go_asleep_time"),
          activeTimes = config.getStringList("biochronometer.active_times").asScala.toList,
          allTimeAtYourService = config.getBoolean("biochronometer.all_time_at_your_service")
        )
      )
    )
  }
}
