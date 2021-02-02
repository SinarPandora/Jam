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
  /**
   * 列出文件结构
   *
   * @param path 目录
   * @return 文件结构
   */
  @GetMapping(Array("/script/{path}"))
  def fileStruct(@PathVariable("path") path: String): ResponseEntity[Object] = ???

  /**
   * 新建脚本
   *
   * @param script 脚本对象
   * @return 创建结果
   */
  @PostMapping(Array("/script"))
  def create(@RequestBody script: SXDLScript): ResponseEntity[Object] = ???

  /**
   * 获取脚本内容
   * *当脚本被锁时返回错误
   *
   * @param path 脚本目录
   * @return 获取结果，若文件被锁，返回 403
   */
  @GetMapping(Array("/script/edit/{path}"))
  def get(@PathVariable("path") path: String): Boolean = ???

  /**
   * 更新脚本内容
   *
   * @param script 脚本对象
   * @return 更新结果，若两人同时编辑，返回 403
   */
  @PutMapping(Array("/script/edit/{path}"))
  def update(@RequestBody script: SXDLScript): ResponseEntity[Object] = ???

  /**
   * 删除脚本
   *
   * @param path 脚本目录
   * @return 删除结果
   */
  @DeleteMapping(Array("/script/{path}"))
  def delete(@PathVariable("path") path: String): ResponseEntity[Object] = ???

  /**
   * 列出指定数量的历史
   *
   * @param dir   目标路径
   * @param count 数量
   * @return 历史列表
   */
  @GetMapping(Array("/script/history/{dir}/{count}"))
  def listAllTags(@PathVariable("dir") dir: String, @PathVariable("count") count: Int): ResponseEntity[Object] = ???

  /**
   * 重命名 tag
   *
   * @param dir 目标路径
   * @param tag 标签名称
   * @return 操作结果
   */
  @PostMapping(Array("/script/history/{dir}"))
  def renameTag(@PathVariable("dir") dir: String, @RequestBody tag: String): ResponseEntity[Object] = ???

  /**
   * 撤回到指定的 tag
   *
   * @param dir 目标路径
   * @param tag 标签名称
   * @return 操作结果
   */
  @PutMapping(Array("/script/history/{dir}"))
  def revertTag(@PathVariable("dir") dir: String, @RequestBody tag: String): ResponseEntity[Object] = ???
}

object EditController {
  private val logger: Logger = ESAPI.getLogger(classOf[EditController])
}
