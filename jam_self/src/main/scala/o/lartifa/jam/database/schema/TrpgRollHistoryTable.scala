package o.lartifa.jam.database.schema

/**
 * Author: sinar
 * 2021/8/15 21:22
 */
// AUTO-GENERATED Slick data model for table TrpgRollHistory
trait TrpgRollHistoryTable {

  self: Tables =>

  import profile.api.*
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR

  /** Entity class storing rows of table TrpgRollHistory
   *
   * @param id         Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param statusId   Database column status_id SqlType(int8)
   * @param result     Database column result SqlType(text)
   * @param point      Database column point SqlType(int4)
   * @param pass       Database column pass SqlType(bool)
   * @param createTime Database column create_time SqlType(timestamp) */
  case class TrpgRollHistoryRow(id: Long, statusId: Long, result: String, point: Int, pass: Boolean, createTime: java.sql.Timestamp)

  /** GetResult implicit for fetching TrpgRollHistoryRow objects using plain SQL queries */
  implicit def GetResultTrpgRollHistoryRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[Boolean], e4: GR[java.sql.Timestamp]): GR[TrpgRollHistoryRow] = GR {
    prs =>
      import prs.*
      TrpgRollHistoryRow.tupled((<<[Long], <<[Long], <<[String], <<[Int], <<[Boolean], <<[java.sql.Timestamp]))
  }

  /** Table description of table trpg_roll_history. Objects of this class serve as prototypes for rows in queries. */
  class TrpgRollHistory(_tableTag: Tag) extends profile.api.Table[TrpgRollHistoryRow](_tableTag, "trpg_roll_history") {
    def * = (id, statusId, result, point, pass, createTime) <> (TrpgRollHistoryRow.tupled, TrpgRollHistoryRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(statusId), Rep.Some(result), Rep.Some(point), Rep.Some(pass), Rep.Some(createTime)).shaped.<>({ r => import r.*; _1.map(_ => TrpgRollHistoryRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column status_id SqlType(int8) */
    val statusId: Rep[Long] = column[Long]("status_id")
    /** Database column result SqlType(text) */
    val result: Rep[String] = column[String]("result")
    /** Database column point SqlType(int4) */
    val point: Rep[Int] = column[Int]("point")
    /** Database column pass SqlType(bool) */
    val pass: Rep[Boolean] = column[Boolean]("pass")
    /** Database column create_time SqlType(timestamp) */
    val createTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_time")

    /** Foreign key referencing TrpgStatus (database name trpg_roll_history_status_id_fkey) */
    lazy val trpgStatusFk = foreignKey("trpg_roll_history_status_id_fkey", statusId, TrpgStatus)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  /** Collection-like TableQuery object for table TrpgRollHistory */
  lazy val TrpgRollHistory = new TableQuery(tag => new TrpgRollHistory(tag))
}
