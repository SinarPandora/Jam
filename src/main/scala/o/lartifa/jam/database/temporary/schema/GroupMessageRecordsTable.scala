package o.lartifa.jam.database.temporary.schema
// AUTO-GENERATED Slick data model for table GroupMessageRecords
trait GroupMessageRecordsTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table GroupMessageRecords
   *  @param id Database column ID SqlType(INTEGER), AutoInc, PrimaryKey
   *  @param message Database column MESSAGE SqlType(VARCHAR)
   *  @param messageId Database column MESSAGE_ID SqlType(INTEGER)
   *  @param messageType Database column MESSAGE_TYPE SqlType(VARCHAR)
   *  @param messageSubType Database column MESSAGE_SUB_TYPE SqlType(VARCHAR)
   *  @param postType Database column POST_TYPE SqlType(VARCHAR)
   *  @param rawMessage Database column RAW_MESSAGE SqlType(VARCHAR)
   *  @param selfId Database column SELF_ID SqlType(INTEGER)
   *  @param senderId Database column SENDER_ID SqlType(INTEGER)
   *  @param groupId Database column GROUP_ID SqlType(INTEGER)
   *  @param isAnonymous Database column IS_ANONYMOUS SqlType(INTEGER)
   *  @param anonymousFlag Database column ANONYMOUS_FLAG SqlType(VARCHAR)
   *  @param anonymousName Database column ANONYMOUS_NAME SqlType(VARCHAR)
   *  @param anonymousId Database column ANONYMOUS_ID SqlType(INTEGER)
   *  @param time Database column TIME SqlType(INTEGER)
   *  @param font Database column FONT SqlType(INTEGER)
   *  @param timestamp Database column TIMESTAMP SqlType(TIMESTAMP) */
  case class GroupMessageRecordsRow(id: Int, message: String, messageId: Int, messageType: String, messageSubType: String, postType: String, rawMessage: String, selfId: Int, senderId: Int, groupId: Int, isAnonymous: Int, anonymousFlag: Option[String], anonymousName: Option[String], anonymousId: Option[Int], time: Int, font: Int, timestamp: java.sql.Timestamp)
  /** GetResult implicit for fetching GroupMessageRecordsRow objects using plain SQL queries */
  implicit def GetResultGroupMessageRecordsRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[String]], e3: GR[Option[Int]], e4: GR[java.sql.Timestamp]): GR[GroupMessageRecordsRow] = GR{
    prs => import prs._
    GroupMessageRecordsRow.tupled((<<[Int], <<[String], <<[Int], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[Int], <<[Int], <<[Int], <<?[String], <<?[String], <<?[Int], <<[Int], <<[Int], <<[java.sql.Timestamp]))
  }
  /** Table description of table GROUP_MESSAGE_RECORDS. Objects of this class serve as prototypes for rows in queries. */
  class GroupMessageRecords(_tableTag: Tag) extends profile.api.Table[GroupMessageRecordsRow](_tableTag, "GROUP_MESSAGE_RECORDS") {
    def * = (id, message, messageId, messageType, messageSubType, postType, rawMessage, selfId, senderId, groupId, isAnonymous, anonymousFlag, anonymousName, anonymousId, time, font, timestamp) <> (GroupMessageRecordsRow.tupled, GroupMessageRecordsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(message), Rep.Some(messageId), Rep.Some(messageType), Rep.Some(messageSubType), Rep.Some(postType), Rep.Some(rawMessage), Rep.Some(selfId), Rep.Some(senderId), Rep.Some(groupId), Rep.Some(isAnonymous), anonymousFlag, anonymousName, anonymousId, Rep.Some(time), Rep.Some(font), Rep.Some(timestamp))).shaped.<>({r=>import r._; _1.map(_=> GroupMessageRecordsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12, _13, _14, _15.get, _16.get, _17.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(INTEGER), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column MESSAGE SqlType(VARCHAR) */
    val message: Rep[String] = column[String]("MESSAGE")
    /** Database column MESSAGE_ID SqlType(INTEGER) */
    val messageId: Rep[Int] = column[Int]("MESSAGE_ID")
    /** Database column MESSAGE_TYPE SqlType(VARCHAR) */
    val messageType: Rep[String] = column[String]("MESSAGE_TYPE")
    /** Database column MESSAGE_SUB_TYPE SqlType(VARCHAR) */
    val messageSubType: Rep[String] = column[String]("MESSAGE_SUB_TYPE")
    /** Database column POST_TYPE SqlType(VARCHAR) */
    val postType: Rep[String] = column[String]("POST_TYPE")
    /** Database column RAW_MESSAGE SqlType(VARCHAR) */
    val rawMessage: Rep[String] = column[String]("RAW_MESSAGE")
    /** Database column SELF_ID SqlType(INTEGER) */
    val selfId: Rep[Int] = column[Int]("SELF_ID")
    /** Database column SENDER_ID SqlType(INTEGER) */
    val senderId: Rep[Int] = column[Int]("SENDER_ID")
    /** Database column GROUP_ID SqlType(INTEGER) */
    val groupId: Rep[Int] = column[Int]("GROUP_ID")
    /** Database column IS_ANONYMOUS SqlType(INTEGER) */
    val isAnonymous: Rep[Int] = column[Int]("IS_ANONYMOUS")
    /** Database column ANONYMOUS_FLAG SqlType(VARCHAR) */
    val anonymousFlag: Rep[Option[String]] = column[Option[String]]("ANONYMOUS_FLAG")
    /** Database column ANONYMOUS_NAME SqlType(VARCHAR) */
    val anonymousName: Rep[Option[String]] = column[Option[String]]("ANONYMOUS_NAME")
    /** Database column ANONYMOUS_ID SqlType(INTEGER) */
    val anonymousId: Rep[Option[Int]] = column[Option[Int]]("ANONYMOUS_ID")
    /** Database column TIME SqlType(INTEGER) */
    val time: Rep[Int] = column[Int]("TIME")
    /** Database column FONT SqlType(INTEGER) */
    val font: Rep[Int] = column[Int]("FONT")
    /** Database column TIMESTAMP SqlType(TIMESTAMP) */
    val timestamp: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("TIMESTAMP")
  }
  /** Collection-like TableQuery object for table GroupMessageRecords */
  lazy val GroupMessageRecords = new TableQuery(tag => new GroupMessageRecords(tag))
}
