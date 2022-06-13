package o.lartifa.jam.plugins.push.scanner

import o.lartifa.jam.plugins.push.source.bilibili.BiliDynamicSourceScanner
import o.lartifa.jam.plugins.push.source.{SourceIdentity, SupportedSource}
import o.lartifa.jam.plugins.push.template.SourceContent

import scala.concurrent.{ExecutionContext, Future}

/**
 * 扫描器
 *
 * Author: sinar
 * 2022/6/12 22:16
 */
trait SourceScanner {
  /**
   * 源扫描
   *
   * @param identity 源标识
   * @param ec       异步上下文
   * @return 扫描结果
   */
  def scan(identity: SourceIdentity)(implicit ec: ExecutionContext): Future[Option[SourceContent]]
}


object SourceScanner {
  val scanners: Map[String, SourceScanner] = Map(
    SupportedSource.BILI_DYNAMIC -> BiliDynamicSourceScanner
  )
}
