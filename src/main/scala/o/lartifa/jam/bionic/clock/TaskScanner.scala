package o.lartifa.jam.bionic.clock

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

import o.lartifa.jam.bionic.task.Task

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.Try

/**
 * 任务扫描器
 *
 * Author: sinar
 * 2020/1/17 23:38
 */
class TaskScanner(tasks: Seq[Task[_]], interval: Duration) {

  private val scanner: AtomicReference[Option[Thread]] = new AtomicReference(None)

  private implicit val exec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  /**
   * 扫描并执行任务
   * （非顺序）
   *
   * @return 执行结果列表
   */
  def scan(): Future[Seq[Any]] = {
    Future.sequence(tasks.map(_.execute()))
  }

  /**
   * 是否扫描任务正在进行
   *
   * @return 是否正在进行
   */
  def isRunning: Boolean = scanner.get().getOrElse(return false).isAlive

  /**
   * 启动扫描任务
   *
   * @return 是否启动成功
   */
  def startLoopScan(): Boolean = {
    if (scanner.get().isEmpty) {
      val thread = new Thread(() => {
        while (true) {
          scan()
          Thread.sleep(interval.toMillis)
        }
      })
      thread.start()
      scanner.getAndSet(Some(thread))
      true
    } else false
  }

  /**
   * 停止扫描任务
   *
   * @return 是否停止成功
   */
  def stop(): Boolean = {
    Try(this.scanner.get().getOrElse(return true).interrupt())
    true
  }

  /**
   * 重启扫描任务
   *
   * @return 是否重启成功
   */
  def restart(): Boolean = {
    scanner.getAndSet(None)
    startLoopScan()
  }
}
