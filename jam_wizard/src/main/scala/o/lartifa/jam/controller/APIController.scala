package o.lartifa.jam.controller

import org.springframework.web.bind.annotation.{GetMapping, RestController}

import java.util.concurrent.CompletionStage
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.FutureConverters._


/**
 * 相关 API
 *
 * Author: sinar
 * 2020/10/8 23:52
 */
@RestController
class APIController {
  @GetMapping(Array("/heartbeat"))
  def heartbeat(): CompletionStage[String] = Future {
    "Aha"
  }.asJava
}
