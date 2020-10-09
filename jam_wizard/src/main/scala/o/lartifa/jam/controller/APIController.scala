package o.lartifa.jam.controller

import org.springframework.web.bind.annotation.{GetMapping, RestController}

/**
 * 相关 API
 *
 * Author: sinar
 * 2020/10/8 23:52
 */
@RestController
class APIController {
  @GetMapping(Array("/heartbeat"))
  def heartbeat(): Boolean = true
}
