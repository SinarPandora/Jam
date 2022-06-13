package o.lartifa.jam.pool

import akka.actor.{ActorRef, ActorSystem}
import cc.moecraft.icq.sender.IcqHttpApi
import cc.moecraft.icq.{PicqBotX, PicqConfig}
import glokka.Registry
import o.lartifa.jam.common.config.{JamConfig, botConfigFile}
import o.lartifa.jam.common.util.LoggerFactory

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
  val actorSystem: ActorSystem = ActorSystem("System", config = botConfigFile)
  val registry: ActorRef = Registry.start(actorSystem, s"${JamConfig.config.name} Proxy")

  private var httpApi: () => IcqHttpApi = _

  def setAPIClient(generator: () => IcqHttpApi): Unit = this.httpApi = generator

  def apiClient: IcqHttpApi = this.httpApi.apply()
}
