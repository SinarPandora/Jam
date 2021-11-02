package o.lartifa.jam.database.schema

/**
 * Author: sinar
 * 2021/8/15 21:22
 */
// AUTO-GENERATED Slick data model for table TrpgActorSnapshot
trait TrpgActorSnapshotTable {

  self: Tables =>

  import profile.api.*
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR

  /** Entity class storing rows of table TrpgActorSnapshot
   *
   * @param id         Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param actorId    Database column actor_id SqlType(int8)
   * @param name       Database column name SqlType(text)
   * @param qid        Database column qid SqlType(int8)
   * @param attr       Database column attr SqlType(text)
   * @param info       Database column info SqlType(text)
   * @param createTime Database column create_time SqlType(timestamp) */
  case class TrpgActorSnapshotRow(id: Long, actorId: Long, name: String, qid: Long, attr: String, info: String, createTime: java.sql.Timestamp)

  /** GetResult implicit for fetching TrpgActorSnapshotRow objects using plain SQL queries */
  implicit def GetResultTrpgActorSnapshotRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[TrpgActorSnapshotRow] = GR {
    prs =>
      import prs.*
      TrpgActorSnapshotRow.tupled((<<[Long], <<[Long], <<[String], <<[Long], <<[String], <<[String], <<[java.sql.Timestamp]))
  }

  /** Table description of table trpg_actor_snapshot. Objects of this class serve as prototypes for rows in queries. */
  class TrpgActorSnapshot(_tableTag: Tag) extends profile.api.Table[TrpgActorSnapshotRow](_tableTag, "trpg_actor_snapshot") {
    def * = (id, actorId, name, qid, attr, info, createTime) <> (TrpgActorSnapshotRow.tupled, TrpgActorSnapshotRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(actorId), Rep.Some(name), Rep.Some(qid), Rep.Some(attr), Rep.Some(info), Rep.Some(createTime)).shaped.<>({ r => import r.*; _1.map(_ => TrpgActorSnapshotRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column create_time SqlType(timestamp) */
    val createTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_time")

    /** Foreign key referencing TrpgActor (database name trpg_actor_snapshot_actor_id_fkey) */
    lazy val trpgActorFk = foreignKey("trpg_actor_snapshot_actor_id_fkey", actorId, TrpgActor)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  /** Collection-like TableQuery object for table TrpgActorSnapshot */
  lazy val TrpgActorSnapshot = new TableQuery(tag => new TrpgActorSnapshot(tag))
}
