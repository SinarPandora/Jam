package o.lartifa.jam.repository

import o.lartifa.jam.model.Instance

import scala.util.Try

/**
 * Jam 实例存储库
 *
 * Author: sinar
 * 2021/1/23 01:16
 */
trait InstanceRepo {
  /**
   * 获取全部实例
   *
   * @return 实例列表
   */
  def selectAll(): List[Instance]

  /**
   * 获取指定数量的实例
   *
   * @param limit 每页数量
   * @param page  当前页标
   * @return 实力列表
   */
  def selectSome(limit: Int, page: Int): List[Instance]

  /**
   * 获取指定实例
   *
   * @param id 实例 ID
   * @return 指定实例
   */
  def selectOne(id: Int): Option[Instance]

  /**
   * 创建实例
   *
   * @param record 记录
   * @return 创建结果
   */
  def create(record: Instance): Try[Instance]

  /**
   * 通过 ID 更新实例
   *
   * @param id     实例 ID
   * @param record 记录
   * @return 更新结果
   */
  def update(id: Int, record: Instance): Try[Instance]

  /**
   * 通过 ID 删除指定实例
   *
   * @param id 实例 ID
   * @return 删除结果
   */
  def delete(id: Int): Try[Boolean]
}
