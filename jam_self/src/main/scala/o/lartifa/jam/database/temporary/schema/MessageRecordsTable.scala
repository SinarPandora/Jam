package o.lartifa.jam.database.temporary.schema
// AUTO-GENERATED Slick data model for table MessageRecords
trait MessageRecordsTable {

  self:Tables  =>

  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table MessageRecords
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param message Database column message SqlType(varchar), Default()
   *  @param messageId Database column message_id SqlType(int8), Default(0)
   *  @param messageType Database column message_type SqlType(varchar), Default()
   *  @param messageSubType Database column message_sub_type SqlType(varchar), Default()
   *  @param postType Database column post_type SqlType(varchar), Default()
   *  @param rawMessage Database column raw_message SqlType(varchar), Default()
   *  @param selfId Database column self_id SqlType(int8), Default(0)
   *  @param senderId Database column sender_id SqlType(int8), Default(0)
   *  @param groupId Database column group_id SqlType(int8), Default(-1)
   *  @param font Database column font SqlType(int8), Default(0)
   *  @param timestamp Database column timestamp SqlType(timestamp) */
  case class MessageRecord(id: Long, message: String = "", messageId: Long = 0L, messageType: String = "", messageSubType: String = "", postType: String = "", rawMessage: String = "", selfId: Long = 0L, senderId: Long = 0L, groupId: Long = -1L, font: Long = 0L, timestamp: java.sql.Timestamp)
  /** GetResult implicit for fetching MessageRecord objects using plain SQL queries */
  implicit def GetResultMessageRecord(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[MessageRecord] = GR{
    prs => import prs._
      MessageRecord.tupled((<<[Long], <<[String], <<[Long], <<[String], <<[String], <<[String], <<[String], <<[Long], <<[Long], <<[Long], <<[Long], <<[java.sql.Timestamp]))
  }
  /** Table description of table message_records. Objects of this class serve as prototypes for rows in queries. */
  class MessageRecords(_tableTag: Tag) extends profile.api.Table[MessageRecord](_tableTag, "message_records") {
    def * = (id, message, messageId, messageType, messageSubType, postType, rawMessage, selfId, senderId, groupId, font, timestamp) <> (MessageRecord.tupled, MessageRecord.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(message), Rep.Some(messageId), Rep.Some(messageType), Rep.Some(messageSubType), Rep.Some(postType), Rep.Some(rawMessage), Rep.Some(selfId), Rep.Some(senderId), Rep.Some(groupId), Rep.Some(font), Rep.Some(timestamp)).shaped.<>({ r=>import r._; _1.map(_=> MessageRecord.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column message SqlType(varchar), Default() */
    val message: Rep[String] = column[String]("message", O.Default(""))
    /** Database column message_id SqlType(int8), Default(0) */
    val messageId: Rep[Long] = column[Long]("message_id", O.Default(0L))
    /** Database column message_type SqlType(varchar), Default() */
    val messageType: Rep[String] = column[String]("message_type", O.Default(""))
    /** Database column message_sub_type SqlType(varchar), Default() */
    val messageSubType: Rep[String] = column[String]("message_sub_type", O.Default(""))
    /** Database column post_type SqlType(varchar), Default() */
    val postType: Rep[String] = column[String]("post_type", O.Default(""))
    /** Database column raw_message SqlType(varchar), Default() */
    val rawMessage: Rep[String] = column[String]("raw_message", O.Default(""))
    /** Database column self_id SqlType(int8), Default(0) */
    val selfId: Rep[Long] = column[Long]("self_id", O.Default(0L))
    /** Database column sender_id SqlType(int8), Default(0) */
    val senderId: Rep[Long] = column[Long]("sender_id", O.Default(0L))
    /** Database column group_id SqlType(int8), Default(-1) */
    val groupId: Rep[Long] = column[Long]("group_id", O.Default(-1L))
    /** Database column font SqlType(int8), Default(0) */
    val font: Rep[Long] = column[Long]("font", O.Default(0L))
    /** Database column timestamp SqlType(timestamp) */
    val timestamp: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("timestamp")
  }
  /** Collection-like TableQuery object for table MessageRecords */
  lazy val MessageRecords = new TableQuery(tag => new MessageRecords(tag))
}
