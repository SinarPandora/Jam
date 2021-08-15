package o.lartifa.jam.database.temporary.schema

/**
 * Author: sinar
 * 2021/8/15 21:23
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
   * @param id            Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param snapshotId    Database column snapshot_id SqlType(int8)
   * @param gameId        Database column game_id SqlType(int8)
   * @param attrOverrides Database column attr_overrides SqlType(text), Default({})
   * @param tags          Database column tags SqlType(text), Default({})
   * @param updateTime    Database column update_time SqlType(timestamp) */
  case class TrpgStatusRow(id: Long, snapshotId: Long, gameId: Long, attrOverrides: String = "{}", tags: String = "{}", updateTime: java.sql.Timestamp)

  /** GetResult implicit for fetching TrpgStatusRow objects using plain SQL queries */
  implicit def GetResultTrpgStatusRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[TrpgStatusRow] = GR {
    prs =>
      import prs._
      TrpgStatusRow.tupled((<<[Long], <<[Long], <<[Long], <<[String], <<[String], <<[java.sql.Timestamp]))
  }

  /** Table description of table trpg_status. Objects of this class serve as prototypes for rows in queries. */
  class TrpgStatus(_tableTag: Tag) extends profile.api.Table[TrpgStatusRow](_tableTag, "trpg_status") {
    def * = (id, snapshotId, gameId, attrOverrides, tags, updateTime) <> (TrpgStatusRow.tupled, TrpgStatusRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(snapshotId), Rep.Some(gameId), Rep.Some(attrOverrides), Rep.Some(tags), Rep.Some(updateTime)).shaped.<>({ r => import r._; _1.map(_ => TrpgStatusRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column snapshot_id SqlType(int8) */
    val snapshotId: Rep[Long] = column[Long]("snapshot_id")
    /** Database column game_id SqlType(int8) */
    val gameId: Rep[Long] = column[Long]("game_id")
    /** Database column attr_overrides SqlType(text), Default({}) */
    val attrOverrides: Rep[String] = column[String]("attr_overrides", O.Default("{}"))
    /** Database column tags SqlType(text), Default({}) */
    val tags: Rep[String] = column[String]("tags", O.Default("{}"))
    /** Database column update_time SqlType(timestamp) */
    val updateTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_time")

    /** Foreign key referencing TrpgActorSnapshot (database name trpg_status_snapshot_id_fkey) */
    lazy val trpgActorSnapshotFk = foreignKey("trpg_status_snapshot_id_fkey", snapshotId, TrpgActorSnapshot)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing TrpgGame (database name trpg_status_game_id_fkey) */
    lazy val trpgGameFk = foreignKey("trpg_status_game_id_fkey", gameId, TrpgGame)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  /** Collection-like TableQuery object for table TrpgStatus */
  lazy val TrpgStatus = new TableQuery(tag => new TrpgStatus(tag))
}
