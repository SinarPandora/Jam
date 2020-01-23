package o.lartifa.jam.bionic

import cn.hutool.cron.CronUtil
import com.typesafe.config.Config
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.engine.parser.Parser
import o.lartifa.jam.model.tasks.{ChangeRespFrequency, GoASleep, WakeUp}

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

  /**
   * 解析
   */
  @throws[ParseFailException]
  def parse(): Unit = {
    val isAllTimeMode = Try(config.getBoolean("all_time_at_your_service"))
      .getOrElse(throw ParseFailException("'全天候模式'需设置为 true 或者 false（没有引号）"))
    if (!isAllTimeMode) {
      parseWakeUp()
      parseGoASleep()
    }
    parseActiveTimes()
  }

  /**
   * 解析起床时间
   */
  private def parseWakeUp(): Unit = {
    val wakeUpTime = Try(config.getInt("wake_up_time"))
      .getOrElse(throw ParseFailException("起床时间必须设置为数字"))
    need(wakeUpTime >= 0 && wakeUpTime <= 23, "起床时间范围需要为 0 - 23")
    CronUtil.schedule(s"0 0 $wakeUpTime * * ? ", WakeUp)
  }

  /**
   * 解析睡眠时间
   */
  private def parseGoASleep(): Unit = {
    val goASleepTime = Try(config.getInt("go_asleep_time"))
      .getOrElse(throw ParseFailException("就寝时间必须设置为数字"))
    need(goASleepTime >= 0 && goASleepTime <= 23, "就寝时间范围应为 0 - 23")
    CronUtil.schedule(s"0 0 $goASleepTime * * ? ", GoASleep)
  }

  /**
   * 解析活跃模式
   */
  private def parseActiveTimes(): Unit = {
    val activeTimes = Try(config.getStringList("active_times").asScala)
    .getOrElse(throw ParseFailException("'活跃时间模式'设置有误，如需设置为不使用，请填写为 [\"None\"]（包括所有符号）"))
    need(activeTimes.lengthIs > 0, "'活跃时间模式'设置有误，如需设置为不使用，请填写为 [\"None\"]（包括所有符号）")
    if (activeTimes.lengthIs == 1 && activeTimes.head == "None") return
    activeTimes.foreach(pair => parseActiveTime(pair).recover(throw _))
  }

  /**
   * 解析每个活跃模式字符串
   *
   * @param pair 字符串
   * @return 解析结果
   */
  private def parseActiveTime(pair: String): Try[Unit] = Try {
    val split = pair.split("-")
    need(split.lengthIs == 2, "活跃时间段应设置为：\"数字-数字\" 的格式")
    val start = Try(split.head.toInt).getOrElse(throw ParseFailException("起始时间必须为数字"))
    need(start >= 0 && start <= 23, "起始时间范围应为 0 - 23")
    val end = Try(split.last.toInt).getOrElse(throw ParseFailException("结束时间必须为数字"))
    need(end >= 0 && end <= 23, "结束时间范围应为 0 - 23")
    CronUtil.schedule(s"0 0 $start * * ? ", ChangeRespFrequency(100))
    CronUtil.schedule(s"0 0 $end * * ? ", ChangeRespFrequency(JamConfig.responseFrequency))
    ()
  } recoverWith { err =>
    val thr = ParseFailException(err.getMessage + s"，错误的时间段为：$pair")
    thr.initCause(err)
    Failure(thr)
  }
}
