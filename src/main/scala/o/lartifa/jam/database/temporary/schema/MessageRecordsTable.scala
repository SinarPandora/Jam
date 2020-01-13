package o.lartifa.jam.database.temporary.schema
// AUTO-GENERATED Slick data model for table MessageRecords
trait MessageRecordsTable {

  self:Tables  =>

  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table MessageRecords
   *  @param id Database column ID SqlType(INTEGER), AutoInc, PrimaryKey
   *  @param message Database column MESSAGE SqlType(VARCHAR)
   *  @param messageId Database column MESSAGE_ID SqlType(BIGINT)
   *  @param messageType Database column MESSAGE_TYPE SqlType(VARCHAR)
   *  @param messageSubType Database column MESSAGE_SUB_TYPE SqlType(VARCHAR)
   *  @param postType Database column POST_TYPE SqlType(VARCHAR)
   *  @param rawMessage Database column RAW_MESSAGE SqlType(VARCHAR)
   *  @param selfId Database column SELF_ID SqlType(BIGINT)
   *  @param senderId Database column SENDER_ID SqlType(BIGINT)
   *  @param groupId Database column GROUP_ID SqlType(BIGINT), Default(-1)
   *  @param font Database column FONT SqlType(BIGINT)
   *  @param timestamp Database column TIMESTAMP SqlType(BIGINT) */
  case class MessageRecord(id: Int, message: String, messageId: Long, messageType: String, messageSubType: String, postType: String, rawMessage: String, selfId: Long, senderId: Long, groupId: Long = -1L, font: Long, timestamp: Long)
  /** GetResult implicit for fetching MessageRecordsRow objects using plain SQL queries */
  implicit def GetResultMessageRecordsRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Long]): GR[MessageRecord] = GR{
    prs => import prs._
      MessageRecord.tupled((<<[Int], <<[String], <<[Long], <<[String], <<[String], <<[String], <<[String], <<[Long], <<[Long], <<[Long], <<[Long], <<[Long]))
  }
  /** Table description of table MESSAGE_RECORDS. Objects of this class serve as prototypes for rows in queries. */
  class MessageRecords(_tableTag: Tag) extends profile.api.Table[MessageRecord](_tableTag, "MESSAGE_RECORDS") {
    def * = (id, message, messageId, messageType, messageSubType, postType, rawMessage, selfId, senderId, groupId, font, timestamp) <> (MessageRecord.tupled, MessageRecord.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(message), Rep.Some(messageId), Rep.Some(messageType), Rep.Some(messageSubType), Rep.Some(postType), Rep.Some(rawMessage), Rep.Some(selfId), Rep.Some(senderId), Rep.Some(groupId), Rep.Some(font), Rep.Some(timestamp))).shaped.<>({r=>import r._; _1.map(_=> MessageRecord.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(INTEGER), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column MESSAGE SqlType(VARCHAR) */
    val message: Rep[String] = column[String]("MESSAGE")
    /** Database column MESSAGE_ID SqlType(BIGINT) */
    val messageId: Rep[Long] = column[Long]("MESSAGE_ID")
    /** Database column MESSAGE_TYPE SqlType(VARCHAR) */
    val messageType: Rep[String] = column[String]("MESSAGE_TYPE")
    /** Database column MESSAGE_SUB_TYPE SqlType(VARCHAR) */
    val messageSubType: Rep[String] = column[String]("MESSAGE_SUB_TYPE")
    /** Database column POST_TYPE SqlType(VARCHAR) */
    val postType: Rep[String] = column[String]("POST_TYPE")
    /** Database column RAW_MESSAGE SqlType(VARCHAR) */
    val rawMessage: Rep[String] = column[String]("RAW_MESSAGE")
    /** Database column SELF_ID SqlType(BIGINT) */
    val selfId: Rep[Long] = column[Long]("SELF_ID")
    /** Database column SENDER_ID SqlType(BIGINT) */
    val senderId: Rep[Long] = column[Long]("SENDER_ID")
    /** Database column GROUP_ID SqlType(BIGINT), Default(-1) */
    val groupId: Rep[Long] = column[Long]("GROUP_ID", O.Default(-1L))
    /** Database column FONT SqlType(BIGINT) */
    val font: Rep[Long] = column[Long]("FONT")
    /** Database column TIMESTAMP SqlType(BIGINT) */
    val timestamp: Rep[Long] = column[Long]("TIMESTAMP")
  }
  /** Collection-like TableQuery object for table MessageRecords */
  lazy val MessageRecords = new TableQuery(tag => new MessageRecords(tag))
}
