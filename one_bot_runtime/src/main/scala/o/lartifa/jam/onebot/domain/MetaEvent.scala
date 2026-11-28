package o.lartifa.jam.onebot.domain

/**
 * 业务侧元事件（heartbeat / lifecycle）。
 *
 * Author: sinar
 * 2026/11/28
 */
sealed trait MetaEvent {
  def postType: String
  def metaEventType: String
  def selfId: Long
  def time: Long
}

final case class MetaHeartbeat(
  postType: String,
  metaEventType: String,
  selfId: Long,
  time: Long,
  interval: Option[Long],
) extends MetaEvent

final case class MetaLifecycle(
  postType: String,
  metaEventType: String,
  selfId: Long,
  time: Long,
  subType: Option[String],
) extends MetaEvent
