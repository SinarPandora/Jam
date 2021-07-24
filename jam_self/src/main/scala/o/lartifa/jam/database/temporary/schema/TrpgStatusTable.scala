package o.lartifa.jam.database.temporary.schema

/**
 * Author: sinar
 * 2021/7/24 21:02
 */
// AUTO-GENERATED Slick data model for table TrpgStatus
trait TrpgStatusTable {

  self: Tables =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** Entity class storing rows of table TrpgStatus
   *
   * @param id         Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param actorId    Database column actor_id SqlType(int8)
   * @param gameId     Database column game_id SqlType(int8)
   * @param attrAdjust Database column attr_adjust SqlType(text), Default({})
   * @param status     Database column status SqlType(text), Default({})
   * @param updateDate Database column update_date SqlType(timestamp) */
  case class TrpgStatusRow(id: Long, actorId: Long, gameId: Long, attrAdjust: String = "{}", status: String = "{}", updateDate: java.sql.Timestamp)

  /** GetResult implicit for fetching TrpgStatusRow objects using plain SQL queries */
  implicit def GetResultTrpgStatusRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[TrpgStatusRow] = GR {
    prs =>
      import prs._
      TrpgStatusRow.tupled((<<[Long], <<[Long], <<[Long], <<[String], <<[String], <<[java.sql.Timestamp]))
  }

  /** Table description of table trpg_status. Objects of this class serve as prototypes for rows in queries. */
  class TrpgStatus(_tableTag: Tag) extends profile.api.Table[TrpgStatusRow](_tableTag, "trpg_status") {
    def * = (id, actorId, gameId, attrAdjust, status, updateDate) <> (TrpgStatusRow.tupled, TrpgStatusRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(actorId), Rep.Some(gameId), Rep.Some(attrAdjust), Rep.Some(status), Rep.Some(updateDate)).shaped.<>({ r => import r._; _1.map(_ => TrpgStatusRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column actor_id SqlType(int8) */
    val actorId: Rep[Long] = column[Long]("actor_id")
    /** Database column game_id SqlType(int8) */
    val gameId: Rep[Long] = column[Long]("game_id")
    /** Database column attr_adjust SqlType(text), Default({}) */
    val attrAdjust: Rep[String] = column[String]("attr_adjust", O.Default("{}"))
    /** Database column status SqlType(text), Default({}) */
    val status: Rep[String] = column[String]("status", O.Default("{}"))
    /** Database column update_date SqlType(timestamp) */
    val updateDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_date")

    /** Foreign key referencing TrpgGame (database name trpg_status_game_id_fkey) */
    lazy val trpgGameFk = foreignKey("trpg_status_game_id_fkey", gameId, TrpgGame)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  /** Collection-like TableQuery object for table TrpgStatus */
  lazy val TrpgStatus = new TableQuery(tag => new TrpgStatus(tag))
}
