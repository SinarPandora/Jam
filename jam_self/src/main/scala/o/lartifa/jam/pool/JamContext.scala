package o.lartifa.jam.pool

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import cc.moecraft.icq.sender.IcqHttpApi
import cc.moecraft.icq.{PicqBotX, PicqConfig}
import o.lartifa.jam.common.util.LoggerFactory
import o.lartifa.jam.model.patterns.ContentMatcher

/**
 * 果酱（系统）上下文
 * TODO: 更优雅的状态管理
 *
 * Author: sinar
 * 2020/1/4 23:52
 */
object JamContext {
  val bot: AtomicReference[PicqBotX] = new AtomicReference[PicqBotX]()
  val editLock: AtomicBoolean = new AtomicBoolean(false)
  val stepPool: AtomicReference[StepPool] = new AtomicReference[StepPool]()
  val cronTaskPool: AtomicReference[CronTaskPool] = new AtomicReference[CronTaskPool]()
  val variablePool: DBVarPool = DBVarPool()
  val globalMatchers: AtomicReference[List[ContentMatcher]] = new AtomicReference[List[ContentMatcher]]()
  val customMatchers: AtomicReference[Map[String, Map[Long, List[ContentMatcher]]]] = new AtomicReference()
  val loggerFactory: AtomicReference[LoggerFactory] = new AtomicReference[LoggerFactory]()
  val messagePool: MessagePool = new MessagePool()
  val clientConfig: AtomicReference[PicqConfig] = new AtomicReference[PicqConfig]()
  val httpApi: AtomicReference[() => IcqHttpApi] = new AtomicReference()
}
