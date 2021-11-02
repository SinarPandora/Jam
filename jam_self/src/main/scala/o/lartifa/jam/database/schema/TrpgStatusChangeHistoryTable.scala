package o.lartifa.jam.database.schema

/**
 * Author: sinar
 * 2021/8/15 21:22
 */
// AUTO-GENERATED Slick data model for table TrpgStatusChangeHistory
trait TrpgStatusChangeHistoryTable {

  self: Tables =>

  import profile.api.*
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR

  /** Entity class storing rows of table TrpgStatusChangeHistory
   *
   * @param id          Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param statusId    Database column status_id SqlType(int8)
   * @param name        Database column name SqlType(text)
   * @param adjustExpr  Database column adjust_expr SqlType(text)
   * @param originValue Database column origin_value SqlType(int4)
   * @param afterValue  Database column after_value SqlType(int4)
   * @param createTime  Database column create_time SqlType(timestamp) */
  case class TrpgStatusChangeHistoryRow(id: Long, statusId: Long, name: String, adjustExpr: String, originValue: Int, afterValue: Int, createTime: java.sql.Timestamp)

  /** GetResult implicit for fetching TrpgStatusChangeHistoryRow objects using plain SQL queries */
  implicit def GetResultTrpgStatusChangeHistoryRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[java.sql.Timestamp]): GR[TrpgStatusChangeHistoryRow] = GR {
    prs =>
      import prs.*
      TrpgStatusChangeHistoryRow.tupled((<<[Long], <<[Long], <<[String], <<[String], <<[Int], <<[Int], <<[java.sql.Timestamp]))
  }

  /** Table description of table trpg_status_change_history. Objects of this class serve as prototypes for rows in queries. */
  class TrpgStatusChangeHistory(_tableTag: Tag) extends profile.api.Table[TrpgStatusChangeHistoryRow](_tableTag, "trpg_status_change_history") {
    def * = (id, statusId, name, adjustExpr, originValue, afterValue, createTime) <> (TrpgStatusChangeHistoryRow.tupled, TrpgStatusChangeHistoryRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(statusId), Rep.Some(name), Rep.Some(adjustExpr), Rep.Some(originValue), Rep.Some(afterValue), Rep.Some(createTime)).shaped.<>({ r => import r.*; _1.map(_ => TrpgStatusChangeHistoryRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column status_id SqlType(int8) */
    val statusId: Rep[Long] = column[Long]("status_id")
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column adjust_expr SqlType(text) */
    val adjustExpr: Rep[String] = column[String]("adjust_expr")
    /** Database column origin_value SqlType(int4) */
    val originValue: Rep[Int] = column[Int]("origin_value")
    /** Database column after_value SqlType(int4) */
    val afterValue: Rep[Int] = column[Int]("after_value")
    /** Database column create_time SqlType(timestamp) */
    val createTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_time")

    /** Foreign key referencing TrpgStatus (database name trpg_status_change_history_status_id_fkey) */
    lazy val trpgStatusFk = foreignKey("trpg_status_change_history_status_id_fkey", statusId, TrpgStatus)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  /** Collection-like TableQuery object for table TrpgStatusChangeHistory */
  lazy val TrpgStatusChangeHistory = new TableQuery(tag => new TrpgStatusChangeHistory(tag))
}
