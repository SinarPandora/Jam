package o.lartifa.jam.database.schema

/**
 * Author: sinar
 * 2021/12/20 22:18
 */
// AUTO-GENERATED Slick data model for table BwySessionHistory
trait BwySessionHistoryTable {

  self: Tables =>

  import profile.api.*
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR

  /** Entity class storing rows of table BwySessionHistory
   *
   * @param id          Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param memberQid   Database column member_qid SqlType(int8)
   * @param sessionId   Database column session_id SqlType(int8)
   * @param sessionType Database column session_type SqlType(int4)
   * @param startTime   Database column start_time SqlType(timestamp)
   * @param endTime     Database column end_time SqlType(timestamp), Default(None) */
  case class BwySessionHistoryRow(id: Long, memberQid: Long, sessionId: Long, sessionType: Int, startTime: java.sql.Timestamp, endTime: Option[java.sql.Timestamp] = None)

  /** GetResult implicit for fetching BwySessionHistoryRow objects using plain SQL queries */
  implicit def GetResultBwySessionHistoryRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[java.sql.Timestamp], e3: GR[Option[java.sql.Timestamp]]): GR[BwySessionHistoryRow] = GR {
    prs =>
      import prs.*
      BwySessionHistoryRow.tupled((<<[Long], <<[Long], <<[Long], <<[Int], <<[java.sql.Timestamp], <<?[java.sql.Timestamp]))
  }

  /** Table description of table bwy_session_history. Objects of this class serve as prototypes for rows in queries. */
  class BwySessionHistory(_tableTag: Tag) extends profile.api.Table[BwySessionHistoryRow](_tableTag, "bwy_session_history") {
    def * = (id, memberQid, sessionId, sessionType, startTime, endTime) <> (BwySessionHistoryRow.tupled, BwySessionHistoryRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(memberQid), Rep.Some(sessionId), Rep.Some(sessionType), Rep.Some(startTime), endTime).shaped.<>({ r => import r.*; _1.map(_ => BwySessionHistoryRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column member_qid SqlType(int8) */
    val memberQid: Rep[Long] = column[Long]("member_qid")
    /** Database column session_id SqlType(int8) */
    val sessionId: Rep[Long] = column[Long]("session_id")
    /** Database column session_type SqlType(int4) */
    val sessionType: Rep[Int] = column[Int]("session_type")
    /** Database column start_time SqlType(timestamp) */
    val startTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("start_time")
    /** Database column end_time SqlType(timestamp), Default(None) */
    val endTime: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("end_time", O.Default(None))
  }
  /** Collection-like TableQuery object for table BwySessionHistory */
  lazy val BwySessionHistory = new TableQuery(tag => new BwySessionHistory(tag))
}
