package o.lartifa.jam.pool

import org.apache.pekko.actor.ActorSystem
import o.lartifa.jam.ob.sender.IcqHttpApi
import o.lartifa.jam.ob.{PicqBotX, PicqConfig}
import o.lartifa.jam.common.config.botConfigFile
import o.lartifa.jam.common.util.LoggerFactory
import o.lartifa.jam.onebot.protocol.api.OutboundGateway

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

/**
 * 果酱（系统）上下文
 * TODO: 更优雅的状态管理
 *
 * Author: sinar
 * 2020/1/4 23:52
 */
object JamContext {
  val bot: AtomicReference[PicqBotX] = new AtomicReference[PicqBotX]()
  val initLock: AtomicBoolean = new AtomicBoolean(false)
  val stepPool: AtomicReference[StepPool] = new AtomicReference[StepPool]()
  val cronTaskPool: AtomicReference[CronTaskPool] = new AtomicReference[CronTaskPool]()
  val variablePool: DBVarPool = DBVarPool()
  val loggerFactory: AtomicReference[LoggerFactory] = new AtomicReference[LoggerFactory]()
  val messagePool: MessagePool = new MessagePool()
  val clientConfig: AtomicReference[PicqConfig] = new AtomicReference[PicqConfig]()
  val outbound: AtomicReference[OutboundGateway] = new AtomicReference[OutboundGateway]()
  val actorSystem: ActorSystem = ActorSystem("System", config = botConfigFile)

  private var httpApi: () => IcqHttpApi = _

  def setAPIClient(generator: () => IcqHttpApi): Unit = this.httpApi = generator

  def apiClient: IcqHttpApi = this.httpApi.apply()
}
