package o.lartifa.jam.database.schema

// AUTO-GENERATED Slick data model for table SourceObserver
trait SourceObserverTable {

  self: Tables =>

  import profile.api.*
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR

  /** Entity class storing rows of table SourceObserver
   *
   * @param id             Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param sourceIdentity Database column source_identity SqlType(text)
   * @param sourceType     Database column source_type SqlType(text)
   * @param chatId         Database column chat_id SqlType(int8)
   * @param chatType       Database column chat_type SqlType(text)
   * @param createTime     Database column create_time SqlType(timestamp)
   * @param isActive       Database column is_active SqlType(bool), Default(true) */
  case class SourceObserverRow(id: Long, sourceIdentity: String, sourceType: String, chatId: Long, chatType: String, createTime: java.sql.Timestamp, isActive: Boolean = true)

  /** GetResult implicit for fetching SourceObserverRow objects using plain SQL queries */
  implicit def GetResultSourceObserverRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp], e3: GR[Boolean]): GR[SourceObserverRow] = GR {
    prs =>
      import prs.*
      SourceObserverRow.tupled((<<[Long], <<[String], <<[String], <<[Long], <<[String], <<[java.sql.Timestamp], <<[Boolean]))
  }

  /** Table description of table source_observer. Objects of this class serve as prototypes for rows in queries. */
  class SourceObserver(_tableTag: Tag) extends profile.api.Table[SourceObserverRow](_tableTag, "source_observer") {
    def * = (id, sourceIdentity, sourceType, chatId, chatType, createTime, isActive) <> (SourceObserverRow.tupled, SourceObserverRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(sourceIdentity), Rep.Some(sourceType), Rep.Some(chatId), Rep.Some(chatType), Rep.Some(createTime), Rep.Some(isActive)).shaped.<>({ r => import r.*; _1.map(_ => SourceObserverRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column source_identity SqlType(text) */
    val sourceIdentity: Rep[String] = column[String]("source_identity")
    /** Database column source_type SqlType(text) */
    val sourceType: Rep[String] = column[String]("source_type")
    /** Database column chat_id SqlType(int8) */
    val chatId: Rep[Long] = column[Long]("chat_id")
    /** Database column chat_type SqlType(text) */
    val chatType: Rep[String] = column[String]("chat_type")
    /** Database column create_time SqlType(timestamp) */
    val createTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_time")
    /** Database column is_active SqlType(bool), Default(true) */
    val isActive: Rep[Boolean] = column[Boolean]("is_active", O.Default(true))
  }
  /** Collection-like TableQuery object for table SourceObserver */
  lazy val SourceObserver = new TableQuery(tag => new SourceObserver(tag))
}
