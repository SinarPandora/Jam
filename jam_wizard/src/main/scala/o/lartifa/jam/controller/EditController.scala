package o.lartifa.jam.controller

import o.lartifa.jam.model.SXDLScript
import org.owasp.esapi.{ESAPI, Logger}
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation._


/**
 * 编辑相关 API
 *
 * Author: sinar
 * 2020/10/8 23:52
 */
@RestController
class EditController {
  // 用 # 替换竖线
  @GetMapping(Array("/script"))
  def listAll(): ResponseEntity[Object] = ???

  @PostMapping(Array("/script"))
  def create(@RequestBody script: SXDLScript): ResponseEntity[Object] = ???

  @GetMapping(Array("/script/edit/{path}"))
  def checkEditAvailable(@PathVariable("path") path: String): Boolean = ???

  @PutMapping(Array("/script/edit/{path}"))
  def update(@RequestBody script: SXDLScript): ResponseEntity[Object] = ???

  @DeleteMapping(Array("/script/{path}"))
  def delete(@PathVariable("path") path: String): ResponseEntity[Object] = ???
}

object EditController {
  private val logger: Logger = ESAPI.getLogger(classOf[EditController])
}
