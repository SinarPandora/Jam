package o.lartifa.jam.controller

import org.springframework.web.bind.annotation.{GetMapping, PostMapping, RestController}

/**
 * 用户 API
 * Author: sinar
 * 2021/1/14 22:20
 */
@RestController
class UserController {
  @PostMapping(Array("/login"))
  def login() = ???

  @GetMapping(Array("/logout"))
  def logout() = ???
}
