package o.lartifa.jam.database.temporary.schema

/**
 * Author: sinar
 * 2021/7/24 21:02
 */
// AUTO-GENERATED Slick data model for table TrpgActorHistory
trait TrpgActorHistoryTable {

  self: Tables =>

  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** Entity class storing rows of table TrpgActorHistory
   *
   * @param id         Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param actorId    Database column actor_id SqlType(int8)
   * @param name       Database column name SqlType(text)
   * @param qid        Database column qid SqlType(int8)
   * @param attr       Database column attr SqlType(text)
   * @param info       Database column info SqlType(text)
   * @param isActive   Database column is_active SqlType(bool), Default(false)
   * @param updateDate Database column update_date SqlType(timestamp) */
  case class TrpgActorHistoryRow(id: Long, actorId: Long, name: String, qid: Long, attr: String, info: String, isActive: Boolean = false, updateDate: java.sql.Timestamp)

  /** GetResult implicit for fetching TrpgActorHistoryRow objects using plain SQL queries */
  implicit def GetResultTrpgActorHistoryRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Boolean], e3: GR[java.sql.Timestamp]): GR[TrpgActorHistoryRow] = GR {
    prs =>
      import prs._
      TrpgActorHistoryRow.tupled((<<[Long], <<[Long], <<[String], <<[Long], <<[String], <<[String], <<[Boolean], <<[java.sql.Timestamp]))
  }

  /** Table description of table trpg_actor_history. Objects of this class serve as prototypes for rows in queries. */
  class TrpgActorHistory(_tableTag: Tag) extends profile.api.Table[TrpgActorHistoryRow](_tableTag, "trpg_actor_history") {
    def * = (id, actorId, name, qid, attr, info, isActive, updateDate) <> (TrpgActorHistoryRow.tupled, TrpgActorHistoryRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(actorId), Rep.Some(name), Rep.Some(qid), Rep.Some(attr), Rep.Some(info), Rep.Some(isActive), Rep.Some(updateDate)).shaped.<>({ r => import r._; _1.map(_ => TrpgActorHistoryRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column actor_id SqlType(int8) */
    val actorId: Rep[Long] = column[Long]("actor_id")
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column qid SqlType(int8) */
    val qid: Rep[Long] = column[Long]("qid")
    /** Database column attr SqlType(text) */
    val attr: Rep[String] = column[String]("attr")
    /** Database column info SqlType(text) */
    val info: Rep[String] = column[String]("info")
    /** Database column is_active SqlType(bool), Default(false) */
    val isActive: Rep[Boolean] = column[Boolean]("is_active", O.Default(false))
    /** Database column update_date SqlType(timestamp) */
    val updateDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_date")
  }

  /** Collection-like TableQuery object for table TrpgActorHistory */
  lazy val TrpgActorHistory = new TableQuery(tag => new TrpgActorHistory(tag))
}
