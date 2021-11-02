package o.lartifa.jam.database.schema

trait VariablesTable {

  self:Tables  =>

  import profile.api.*
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR
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
    prs => import prs.*
    VariablesRow.tupled((<<[Long], <<[String], <<[String], <<[Long], <<[String], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table variables. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class Variables(_tableTag: Tag) extends profile.api.Table[VariablesRow](_tableTag, "variables") {
    def * = (id, name, chatType, chatId, value, `type`, lastUpdateDate) <> (VariablesRow.tupled, VariablesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name), Rep.Some(chatType), Rep.Some(chatId), Rep.Some(value), Rep.Some(`type`), Rep.Some(lastUpdateDate))).shaped.<>({r=>import r.*; _1.map(_=> VariablesRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text), Default() */
    val name: Rep[String] = column[String]("name", O.Default(""))
    /** Database column chat_type SqlType(text), Default() */
    val chatType: Rep[String] = column[String]("chat_type", O.Default(""))
    /** Database column chat_id SqlType(int8), Default(-1) */
    val chatId: Rep[Long] = column[Long]("chat_id", O.Default(-1L))
    /** Database column value SqlType(text), Default() */
    val value: Rep[String] = column[String]("value", O.Default(""))
    /** Database column type SqlType(text), Default(TEXT)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[String] = column[String]("type", O.Default("TEXT"))
    /** Database column last_update_date SqlType(timestamp) */
    val lastUpdateDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("last_update_date")
  }
  /** Collection-like TableQuery object for table Variables */
  lazy val Variables = new TableQuery(tag => new Variables(tag))
}
