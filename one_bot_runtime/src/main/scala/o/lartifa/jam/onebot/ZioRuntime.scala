package o.lartifa.jam.onebot

import zio.*
import zio.http.Client

/**
 * 同步边界：从非 ZIO 代码（Picq / Bootloader）跑 ZIO 效果。
 *
 * Author: sinar
 * 2026/11/26
 */
object ZioRuntime {
  private val runtime: Runtime[Any] = Runtime.default

  def run[A](effect: ZIO[Any, Any, A]): A =
    Unsafe.unsafe { implicit u =>
      runtime.unsafe
        .run(effect.mapError {
          case t: Throwable => t
          case other        => new RuntimeException(String.valueOf(other))
        })
        .getOrThrowFiberFailure()
    }

  def runWithClient[A](effect: ZIO[Client, Any, A]): A =
    run(effect.provide(Client.default))

  /** 后台 fork，用于常驻入站 Server。 */
  def forkDaemon[E, A](effect: ZIO[Any, E, A]): Fiber.Runtime[E, A] =
    Unsafe.unsafe { implicit u =>
      runtime.unsafe.fork(effect)
    }
}
