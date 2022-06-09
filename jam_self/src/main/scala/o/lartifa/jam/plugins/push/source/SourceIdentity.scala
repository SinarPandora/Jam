package o.lartifa.jam.plugins.push.source

/**
 * 资源标识
 *
 * Author: sinar
 * 2022/6/3 14:45
 */
case class SourceIdentity(sourceType: String, sourceIdentity: String) {
  override def toString: String = s"资源类型：$sourceType, 资源标识：$sourceIdentity"
}
