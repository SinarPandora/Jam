package o.lartifa.jam.controller

import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}

/**
 * Author: sinar
 * 2020/10/9 13:04
 */
@RequestMapping(Array("/system"))
@RestController
class SystemController {
  /**
   * 系统心跳事件
   *
   * @return true
   */
  @GetMapping(Array("/heartbeat"))
  def heartbeat(): Boolean = true
}
