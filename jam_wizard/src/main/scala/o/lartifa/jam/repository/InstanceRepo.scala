package o.lartifa.jam.repository

import io.getquill.{PostgresJdbcContext, SnakeCase}
import o.lartifa.jam.model.Instance
import org.springframework.stereotype.Component

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

@Component
class DefaultInstanceRepo(dataSource: PostgresJdbcContext[SnakeCase]) extends InstanceRepo {

  import dataSource._

  /**
   * 获取全部实例
   *
   * @return 实例列表
   */
  override def selectAll(): List[Instance] = run {
    quote {
      query[Instance]
    }
  }

  /**
   * 获取指定数量的实例
   *
   * @param limit 每页数量
   * @param page  当前页标
   * @return 实力列表
   */
  override def selectSome(limit: Int, page: Int): List[Instance] = {
    if (page <= 0) Nil
    else run {
      quote {
        query[Instance].drop(lift((page - 1) * limit)).take(lift(limit))
      }
    }
  }

  /**
   * 获取指定实例
   *
   * @param id 实例 ID
   * @return 指定实例
   */
  override def selectOne(id: Int): Option[Instance] = run {
    quote {
      query[Instance].filter(_.id == lift(id)).take(1)
    }
  }.headOption

  /**
   * 创建实例
   *
   * @param record 记录
   * @return 创建结果
   */
  override def create(record: Instance): Try[Instance] = {
    Try {
      val id = run {
        quote {
          query[Instance].insert(
            _.name -> lift(record.name),
            _.backendType -> lift(record.backendType),
            _.version -> lift(record.version),
            _.deployPath -> lift(record.deployPath)
          )
        }.returningGenerated(_.id)
      }
      record.copy(id = id, managerIds = Nil, args = "")
    }
  }

  /**
   * 通过 ID 更新实例
   *
   * @param id     实例 ID
   * @param record 记录
   * @return 更新结果
   */
  override def update(id: Int, record: Instance): Try[Instance] = {
    Try {
      val updateAt = run {
        quote {
          query[Instance].filter(_.id == lift(id)).update(
            _.name -> lift(record.name),
            _.version -> lift(record.version),
            _.deployPath -> lift(record.deployPath),
            _.managerIds -> lift(record.managerIds),
            _.backendType -> lift(record.backendType),
            _.args -> lift(record.args)
          )
            .returning(_.updatedAt)
        }
      }
      record.copy(updatedAt = updateAt)
    }
  }

  /**
   * 通过 ID 删除指定实例
   *
   * @param id 实例 ID
   * @return 删除结果
   */
  override def delete(id: Int): Try[Boolean] = {
    Try {
      run {
        quote {
          query[Instance].filter(_.id == lift(id)).delete
        }
      }
      true
    }
  }
}
