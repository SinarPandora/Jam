package o.lartifa.jam.pool

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import cc.moecraft.icq.PicqConfig
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.bionic.Biochronometer
import o.lartifa.jam.model.patterns.ContentMatcher

/**
 * 果酱（系统）上下文
 *
 * Author: sinar
 * 2020/1/4 23:52 
 */
object JamContext {
  val editLock: AtomicBoolean = new AtomicBoolean(false)
  val stepPool: AtomicReference[StepPool] = new AtomicReference[StepPool]()
  val variablePool: VariablePool = new VariablePool()
  val matchers: AtomicReference[List[ContentMatcher]] = new AtomicReference[List[ContentMatcher]]()
  val logger: AtomicReference[HyLogger] = new AtomicReference[HyLogger]()
  val clock: Biochronometer = Biochronometer()
  val messagePool: MessagePool = new MessagePool()
  val clientConfig: AtomicReference[PicqConfig] = new AtomicReference[PicqConfig]()
}
