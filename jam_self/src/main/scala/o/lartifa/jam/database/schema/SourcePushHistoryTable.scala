package o.lartifa.jam.database.schema

// AUTO-GENERATED Slick data model for table SourcePushHistory
trait SourcePushHistoryTable {

  self: Tables =>

  import profile.api.*
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR

  /** Entity class storing rows of table SourcePushHistory
   *
   * @param id           Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param subscriberId Database column subscriber_id SqlType(int8)
   * @param pushTime     Database column push_time SqlType(timestamp)
   * @param messageKey   Database column message_key SqlType(text) */
  case class SourcePushHistoryRow(id: Long, subscriberId: Long, pushTime: java.sql.Timestamp, messageKey: String)

  /** GetResult implicit for fetching SourcePushHistoryRow objects using plain SQL queries */
  implicit def GetResultSourcePushHistoryRow(implicit e0: GR[Long], e1: GR[java.sql.Timestamp], e2: GR[String]): GR[SourcePushHistoryRow] = GR {
    prs =>
      import prs.*
      SourcePushHistoryRow.tupled((<<[Long], <<[Long], <<[java.sql.Timestamp], <<[String]))
  }

  /** Table description of table source_push_history. Objects of this class serve as prototypes for rows in queries. */
  class SourcePushHistory(_tableTag: Tag) extends profile.api.Table[SourcePushHistoryRow](_tableTag, "source_push_history") {
    def * = (id, subscriberId, pushTime, messageKey) <> (SourcePushHistoryRow.tupled, SourcePushHistoryRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(subscriberId), Rep.Some(pushTime), Rep.Some(messageKey)).shaped.<>({ r => import r.*; _1.map(_ => SourcePushHistoryRow.tupled((_1.get, _2.get, _3.get, _4.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column subscriber_id SqlType(int8) */
    val subscriberId: Rep[Long] = column[Long]("subscriber_id")
    /** Database column push_time SqlType(timestamp) */
    val pushTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("push_time")
    /** Database column message_key SqlType(text) */
    val messageKey: Rep[String] = column[String]("message_key")

    /** Foreign key referencing SourceSubscriber (database name source_push_history_subscriber_id_fkey) */
    lazy val sourceSubscriberFk = foreignKey("source_push_history_subscriber_id_fkey", subscriberId, SourceSubscriber)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    /** Uniqueness Index over (subscriberId,messageKey) (database name source_push_history_sid_key) */
    val index1 = index("source_push_history_sid_key", (subscriberId, messageKey), unique = true)
  }
  /** Collection-like TableQuery object for table SourcePushHistory */
  lazy val SourcePushHistory = new TableQuery(tag => new SourcePushHistory(tag))
}
