package o.lartifa.jam.database.schema

/**
 * Author: sinar
 * 2021/12/20 22:19
 */
// AUTO-GENERATED Slick data model for table BwyNotification
trait BwyNotificationTable {

  self: Tables =>

  import profile.api.*
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR

  /** Entity class storing rows of table BwyNotification
   *
   * @param id         Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param content    Database column content SqlType(text)
   * @param cron       Database column cron SqlType(text)
   * @param scope      Database column scope SqlType(text)
   * @param scopeType  Database column scope_type SqlType(int4)
   * @param createTime Database column create_time SqlType(timestamp)
   * @param creatorQid Database column creator_qid SqlType(int8)
   * @param isActive   Database column is_active SqlType(bool), Default(true) */
  case class BwyNotificationRow(id: Long, content: String, cron: String, scope: String, scopeType: Int, createTime: java.sql.Timestamp, creatorQid: Long, isActive: Boolean = true)

  /** GetResult implicit for fetching BwyNotificationRow objects using plain SQL queries */
  implicit def GetResultBwyNotificationRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[java.sql.Timestamp], e4: GR[Boolean]): GR[BwyNotificationRow] = GR {
    prs =>
      import prs.*
      BwyNotificationRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[Int], <<[java.sql.Timestamp], <<[Long], <<[Boolean]))
  }

  /** Table description of table bwy_notification. Objects of this class serve as prototypes for rows in queries. */
  class BwyNotification(_tableTag: Tag) extends profile.api.Table[BwyNotificationRow](_tableTag, "bwy_notification") {
    def * = (id, content, cron, scope, scopeType, createTime, creatorQid, isActive) <> (BwyNotificationRow.tupled, BwyNotificationRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(content), Rep.Some(cron), Rep.Some(scope), Rep.Some(scopeType), Rep.Some(createTime), Rep.Some(creatorQid), Rep.Some(isActive)).shaped.<>({ r => import r.*; _1.map(_ => BwyNotificationRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column content SqlType(text) */
    val content: Rep[String] = column[String]("content")
    /** Database column cron SqlType(text) */
    val cron: Rep[String] = column[String]("cron")
    /** Database column scope SqlType(text) */
    val scope: Rep[String] = column[String]("scope")
    /** Database column scope_type SqlType(int4) */
    val scopeType: Rep[Int] = column[Int]("scope_type")
    /** Database column create_time SqlType(timestamp) */
    val createTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_time")
    /** Database column creator_qid SqlType(int8) */
    val creatorQid: Rep[Long] = column[Long]("creator_qid")
    /** Database column is_active SqlType(bool), Default(true) */
    val isActive: Rep[Boolean] = column[Boolean]("is_active", O.Default(true))
  }
  /** Collection-like TableQuery object for table BwyNotification */
  lazy val BwyNotification = new TableQuery(tag => new BwyNotification(tag))
}
