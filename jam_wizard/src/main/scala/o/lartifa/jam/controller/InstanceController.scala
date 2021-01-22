package o.lartifa.jam.controller

import o.lartifa.jam.common.util.RespMsg._
import o.lartifa.jam.controller.InstanceController.logger
import o.lartifa.jam.model.Instance
import o.lartifa.jam.repository.InstanceRepo
import org.slf4j.{Logger, LoggerFactory}
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

  @GetMapping(Array("/instance/{id}"))
  def get(@PathVariable("id") id: Int): ResponseEntity[Object] =
    repo.selectOne(id) match {
      case Some(value) => ResponseEntity.ok(done(value))
      case None => ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("没有找到指定的实例"))
    }

  @PostMapping(Array("/instance"))
  def create(@RequestBody record: Instance): ResponseEntity[Object] = {
    if (!Instance.validateCreate(record))
      ResponseEntity.badRequest.body(error("实例数据不合法"))
    else repo.create(record) match {
      case Failure(exception) =>
        logger.error("创建实例失败", exception)
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error("创建实例失败"))
      case Success(value) =>
        ResponseEntity.ok(done(value))
    }
  }

  @PutMapping(Array("/instance/{id}"))
  def update(@PathVariable("id") id: Int, @RequestBody record: Instance): ResponseEntity[Object] = {
    if (!Instance.validateUpdate(record))
      ResponseEntity.badRequest.body(error("实例数据不合法"))
    else repo.update(id, record) match {
      case Failure(exception) =>
        logger.error("更新实例失败", exception)
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error("创建实例失败"))
      case Success(value) =>
        ResponseEntity.ok(done(value))
    }
  }

  @DeleteMapping(Array("/instance/{id}"))
  def delete(@PathVariable("id") id: Int): ResponseEntity[Object] = {
    repo.delete(id) match {
      case Failure(exception) =>
        logger.error("删除实例失败", exception)
        ResponseEntity.badRequest.body(error("删除实例失败"))
      case Success(_) =>
        ResponseEntity.ok(done(true))
    }
  }
}

object InstanceController {
  private val logger: Logger = LoggerFactory.getLogger(classOf[InstanceController])
}
