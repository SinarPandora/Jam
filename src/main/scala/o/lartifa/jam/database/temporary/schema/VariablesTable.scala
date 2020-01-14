package o.lartifa.jam.database.temporary.schema
trait VariablesTable {

  self:Tables  =>

  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Variables
   *  @param id Database column ID SqlType(BIGINT), AutoInc, PrimaryKey
   *  @param name Database column NAME SqlType(VARCHAR)
   *  @param chatType Database column CHAT_TYPE SqlType(VARCHAR)
   *  @param chatId Database column CHAT_ID SqlType(BIGINT)
   *  @param value Database column VALUE SqlType(VARCHAR), Default()
   *  @param `type` Database column TYPE SqlType(VARCHAR), Length(10,true), Default(TEXT)
   *  @param lastUpdateDate Database column LAST_UPDATE_DATE SqlType(TIMESTAMP) */
  case class VariablesRow(id: Long, name: String, chatType: String, chatId: Long, value: String = "", `type`: String = "TEXT", lastUpdateDate: java.sql.Timestamp)
  /** GetResult implicit for fetching VariablesRow objects using plain SQL queries */
  implicit def GetResultVariablesRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[VariablesRow] = GR{
    prs => import prs._
    VariablesRow.tupled((<<[Long], <<[String], <<[String], <<[Long], <<[String], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table VARIABLES. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class Variables(_tableTag: Tag) extends profile.api.Table[VariablesRow](_tableTag, "VARIABLES") {
    def * = (id, name, chatType, chatId, value, `type`, lastUpdateDate) <> (VariablesRow.tupled, VariablesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name), Rep.Some(chatType), Rep.Some(chatId), Rep.Some(value), Rep.Some(`type`), Rep.Some(lastUpdateDate))).shaped.<>({r=>import r._; _1.map(_=> VariablesRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(BIGINT), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column NAME SqlType(VARCHAR) */
    val name: Rep[String] = column[String]("NAME")
    /** Database column CHAT_TYPE SqlType(VARCHAR) */
    val chatType: Rep[String] = column[String]("CHAT_TYPE")
    /** Database column CHAT_ID SqlType(BIGINT) */
    val chatId: Rep[Long] = column[Long]("CHAT_ID")
    /** Database column VALUE SqlType(VARCHAR), Default() */
    val value: Rep[String] = column[String]("VALUE", O.Default(""))
    /** Database column TYPE SqlType(VARCHAR), Length(10,true), Default(TEXT)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[String] = column[String]("TYPE", O.Length(10,varying=true), O.Default("TEXT"))
    /** Database column LAST_UPDATE_DATE SqlType(TIMESTAMP) */
    val lastUpdateDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("LAST_UPDATE_DATE")
  }
  /** Collection-like TableQuery object for table Variables */
  lazy val Variables = new TableQuery(tag => new Variables(tag))
}
