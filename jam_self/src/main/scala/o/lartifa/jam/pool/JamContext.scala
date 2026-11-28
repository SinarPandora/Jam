package o.lartifa.jam.pool

import org.apache.pekko.actor.ActorSystem
import o.lartifa.jam.common.config.botConfigFile
import o.lartifa.jam.common.util.LoggerFactory
import o.lartifa.jam.onebot.JamBot
import o.lartifa.jam.onebot.protocol.api.{BotApi, OutboundGateway}

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

/**
 * 果酱（系统）上下文
 * TODO: 更优雅的状态管理
 *
 * Author: sinar
 * 2020/1/4 23:52
 */
object JamContext {
  val bot: AtomicReference[JamBot] = new AtomicReference[JamBot]()
  val initLock: AtomicBoolean = new AtomicBoolean(false)
  val stepPool: AtomicReference[StepPool] = new AtomicReference[StepPool]()
  val cronTaskPool: AtomicReference[CronTaskPool] = new AtomicReference[CronTaskPool]()
  val variablePool: DBVarPool = DBVarPool()
  val loggerFactory: AtomicReference[LoggerFactory] = new AtomicReference[LoggerFactory]()
  val messagePool: MessagePool = new MessagePool()
  val outbound: AtomicReference[OutboundGateway] = new AtomicReference[OutboundGateway]()
  val actorSystem: ActorSystem = ActorSystem("System", config = botConfigFile)

  private var httpApi: () => BotApi = _

  def setAPIClient(generator: () => BotApi): Unit = this.httpApi = generator

  def apiClient: BotApi = this.httpApi.apply()
}
