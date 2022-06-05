package o.lartifa.jam.database.schema

// AUTO-GENERATED Slick data model for table SourceSubscriber
trait SourceSubscriberTable {

  self: Tables =>

  import profile.api.*
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR

  /** Entity class storing rows of table SourceSubscriber
   *
   * @param id             Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param chatId         Database column chat_id SqlType(int8)
   * @param chatType       Database column chat_type SqlType(text)
   * @param sourceId       Database column source_id SqlType(int8)
   * @param isPaused       Database column is_paused SqlType(bool), Default(false)
   * @param lastKey        Database column last_key SqlType(text), Default(INIT)
   * @param lastUpdateTime Database column last_update_time SqlType(timestamp)
   * @param createTime     Database column create_time SqlType(timestamp)
   * @param isActive       Database column is_active SqlType(bool), Default(true) */
  case class SourceSubscriberRow(id: Long, chatId: Long, chatType: String, sourceId: Long, isPaused: Boolean = false, lastKey: String = "INIT", lastUpdateTime: java.sql.Timestamp, createTime: java.sql.Timestamp, isActive: Boolean = true)

  /** GetResult implicit for fetching SourceSubscriberRow objects using plain SQL queries */
  implicit def GetResultSourceSubscriberRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Boolean], e3: GR[java.sql.Timestamp]): GR[SourceSubscriberRow] = GR {
    prs =>
      import prs.*
      SourceSubscriberRow.tupled((<<[Long], <<[Long], <<[String], <<[Long], <<[Boolean], <<[String], <<[java.sql.Timestamp], <<[java.sql.Timestamp], <<[Boolean]))
  }
  /** Table description of table source_subscriber. Objects of this class serve as prototypes for rows in queries. */
  class SourceSubscriber(_tableTag: Tag) extends profile.api.Table[SourceSubscriberRow](_tableTag, "source_subscriber") {
    def * = (id, chatId, chatType, sourceId, isPaused, lastKey, lastUpdateTime, createTime, isActive) <> (SourceSubscriberRow.tupled, SourceSubscriberRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(chatId), Rep.Some(chatType), Rep.Some(sourceId), Rep.Some(isPaused), Rep.Some(lastKey), Rep.Some(lastUpdateTime), Rep.Some(createTime), Rep.Some(isActive)).shaped.<>({ r => import r.*; _1.map(_ => SourceSubscriberRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column chat_id SqlType(int8) */
    val chatId: Rep[Long] = column[Long]("chat_id")
    /** Database column chat_type SqlType(text) */
    val chatType: Rep[String] = column[String]("chat_type")
    /** Database column source_id SqlType(int8) */
    val sourceId: Rep[Long] = column[Long]("source_id")
    /** Database column is_paused SqlType(bool), Default(false) */
    val isPaused: Rep[Boolean] = column[Boolean]("is_paused", O.Default(false))
    /** Database column last_key SqlType(text), Default(INIT) */
    val lastKey: Rep[String] = column[String]("last_key", O.Default("INIT"))
    /** Database column last_update_time SqlType(timestamp) */
    val lastUpdateTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("last_update_time")
    /** Database column create_time SqlType(timestamp) */
    val createTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_time")
    /** Database column is_active SqlType(bool), Default(true) */
    val isActive: Rep[Boolean] = column[Boolean]("is_active", O.Default(true))

    /** Foreign key referencing SourceObserver (database name source_subscriber_source_id_fkey) */
    lazy val sourceObserverFk = foreignKey("source_subscriber_source_id_fkey", sourceId, SourceObserver)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

    /** Uniqueness Index over (chatId,chatType,sourceId) (database name source_subscriber_source_chat) */
    val index1 = index("source_subscriber_source_chat", (chatId, chatType, sourceId), unique = true)
  }
  /** Collection-like TableQuery object for table SourceSubscriber */
  lazy val SourceSubscriber = new TableQuery(tag => new SourceSubscriber(tag))
}
