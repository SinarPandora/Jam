package o.lartifa.jam.bionic

import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import com.typesafe.config.Config
import o.lartifa.jam.common.config.{JamCharacter, JamConfig}
import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.engine.ssdl.parser.Parser
import o.lartifa.jam.model.tasks.{ChangeRespFrequency, GoASleep}
import o.lartifa.jam.pool.JamContext

import java.time.LocalTime
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Try}

/**
 * 生物钟配置解析器
 *
 * Author: sinar
 * 2020/1/23 15:19
 */
object BiochronometerParser extends Parser {
  private val config: Config = JamConfig.config.getConfig("biochronometer")
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(BiochronometerParser.getClass)

  /**
   * 解析
   */
  @throws[ParseFailException]
  def parse(): Unit = {
    val isAllTimeMode = Try(config.getBoolean("all_time_at_your_service"))
      .getOrElse(throw ParseFailException("'全天候模式'需设置为 true 或者 false（没有引号）"))
    if (!isAllTimeMode) {
      val now = LocalTime.now().getHour
      val wakeAt = parseWakeUp()
      val sleepAt = parseGoASleep()
      need(wakeAt != sleepAt, s"起床时间不能和就寝时间相同，${JamConfig.name}的行为可能不正常，建议立刻修改并重新启动")
      // 如果在睡眠期间被重新唤醒，会继续睡
      val continueSleep = if (wakeAt < sleepAt && (wakeAt > now || now >= sleepAt)) true
      else if (sleepAt == 0 && (wakeAt > now || now == 0)) true
      else if (now >= sleepAt && now < wakeAt) true
      else false
      if (continueSleep) {
        MasterUtil.notifyMaster(JamCharacter.ForMaster.goodNight)
        GoASleep.goASleep()
      }
    }
    parseActiveTimes()
  }

  /**
   * 解析起床时间
   */
  private def parseWakeUp(): Int = {
    val wakeUpTime = Try(config.getInt("wake_up_time"))
      .getOrElse(throw ParseFailException("起床时间必须设置为数字"))
    need(wakeUpTime >= 0 && wakeUpTime <= 23, "起床时间范围需要为 0 - 23")
    JamContext.cronTaskPool.get().getActiveTasks("起床").left
      .getOrElse(throw ParseFailException("起床任务尚未初始化"))
      .setUp(s"0 0 $wakeUpTime * * ? ")
    logger.log(s"${AnsiColor.GREEN}起床时间设置为每日${wakeUpTime}时")
    wakeUpTime
  }

  /**
   * 解析睡眠时间
   */
  private def parseGoASleep(): Int = {
    val goASleepTime = Try(config.getInt("go_asleep_time"))
      .getOrElse(throw ParseFailException("就寝时间必须设置为数字"))
    need(goASleepTime >= 0 && goASleepTime <= 23, "就寝时间范围应为 0 - 23")
    JamContext.cronTaskPool.get().getActiveTasks("睡眠").left
      .getOrElse(throw ParseFailException("睡眠任务尚未初始化"))
      .setUp(s"0 0 $goASleepTime * * ? ")
    logger.log(s"${AnsiColor.GREEN}睡眠时间设置为每日${goASleepTime}时")
    goASleepTime
  }

  /**
   * 解析活跃模式
   */
  private def parseActiveTimes(): Unit = {
    val activeTimes = Try(config.getStringList("active_times").asScala)
      .getOrElse(throw ParseFailException("'活跃时间模式'设置有误，如需设置为不使用，请填写为 [\"None\"]（包括所有符号）"))
    need(activeTimes.lengthIs > 0, "'活跃时间模式'设置有误，如需设置为不使用，请填写为 [\"None\"]（包括所有符号）")
    if (activeTimes.lengthIs == 1 && activeTimes.head == "None") return
    activeTimes.foreach(pair => logger.log(s"${AnsiColor.GREEN}活跃时间段添加：${parseActiveTime(pair).recover(throw _).get}时"))
  }

  /**
   * 解析每个活跃模式字符串
   *
   * @param pair 字符串
   * @return 解析结果
   */
  private def parseActiveTime(pair: String): Try[String] = Try {
    val split = pair.split("-")
    need(split.lengthIs == 2, "活跃时间段应设置为：\"数字-数字\" 的格式")
    val start = Try(split.head.toInt).getOrElse(throw ParseFailException("起始时间必须为数字"))
    need(start >= 0 && start <= 23, "起始时间范围应为 0 - 23")
    val end = Try(split.last.toInt).getOrElse(throw ParseFailException("结束时间必须为数字"))
    need(end >= 0 && end <= 23, "结束时间范围应为 0 - 23")
    need(end > start, "结束时间必须大于起始时间")
    ChangeRespFrequency(100).setUp(s"0 0 $start * * ? ")
    ChangeRespFrequency(JamConfig.responseFrequency).setUp(s"0 0 $end * * ? ")
    pair
  } recoverWith { err =>
    val thr = ParseFailException("'活跃时间模式'设置有误，" + err.getMessage + s"，错误的时间段为：$pair")
    thr.initCause(err)
    Failure(thr)
  }
}
