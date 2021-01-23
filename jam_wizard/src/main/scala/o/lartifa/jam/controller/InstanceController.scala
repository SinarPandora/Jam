package o.lartifa.jam.controller

import o.lartifa.jam.common.util.RespMsg._
import o.lartifa.jam.controller.InstanceController.logger
import o.lartifa.jam.model.Instance
import o.lartifa.jam.repository.InstanceRepo
import org.owasp.esapi.{ESAPI, Logger}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation._

import scala.util.{Failure, Success}

/**
 * 实例 API
 *
 * Author: sinar
 * 2021/1/20 21:59
 */
@RestController
class InstanceController(repo: InstanceRepo) {

  /**
   * 获取全部实例
   *
   * @return 实例列表
   */
  @GetMapping(Array("/instance"))
  def listAll(): ResponseEntity[Object] =
    ResponseEntity.ok(done(repo.selectAll()))

  /**
   * 获取指定实例
   *
   * @param id 实例 ID
   * @return 实例数据
   */
  @GetMapping(Array("/instance/{id}"))
  def get(@PathVariable("id") id: Int): ResponseEntity[Object] =
    repo.selectOne(id) match {
      case Some(value) => ResponseEntity.ok(done(value))
      case None => ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("没有找到指定的实例"))
    }

  /**
   * 创建实例
   *
   * @param record 实例记录
   * @return 创建结果
   */
  @PostMapping(Array("/instance"))
  def create(@RequestBody record: Instance): ResponseEntity[Object] = {
    if (!Instance.validateCreate(record))
      ResponseEntity.badRequest.body(error("实例数据不合法"))
    else repo.create(record) match {
      case Failure(exception) =>
        logger.error(Logger.EVENT_FAILURE, "创建实例失败", exception)
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error("创建实例失败"))
      case Success(value) =>
        ResponseEntity.ok(done(value))
    }
  }

  /**
   * 更新实例
   *
   * @param id     实例 ID
   * @param record 实例数据
   * @return 更新结果
   */
  @PutMapping(Array("/instance/{id}"))
  def update(@PathVariable("id") id: Int, @RequestBody record: Instance): ResponseEntity[Object] = {
    if (!Instance.validateUpdate(record))
      ResponseEntity.badRequest.body(error("实例数据不合法"))
    else repo.update(id, record) match {
      case Failure(exception) =>
        logger.error(Logger.EVENT_FAILURE, "更新实例失败", exception)
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error("创建实例失败"))
      case Success(value) =>
        ResponseEntity.ok(done(value))
    }
  }

  /**
   * 删除实例
   *
   * @param id 实例 ID
   * @return 是否成功删除
   */
  @DeleteMapping(Array("/instance/{id}"))
  def delete(@PathVariable("id") id: Int): ResponseEntity[Object] = {
    repo.delete(id) match {
      case Failure(exception) =>
        logger.error(Logger.EVENT_FAILURE, "删除实例失败", exception)
        ResponseEntity.badRequest.body(error("删除实例失败"))
      case Success(_) =>
        ResponseEntity.ok(done(true))
    }
  }
}

object InstanceController {
  private val logger: Logger = ESAPI.getLogger(classOf[InstanceController])
}
