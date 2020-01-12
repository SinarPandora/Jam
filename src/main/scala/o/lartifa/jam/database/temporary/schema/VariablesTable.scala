package o.lartifa.jam.database.temporary.schema

// AUTO-GENERATED Slick data model for table Variables
trait VariablesTable {

  self: Tables =>

  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** Entity class storing rows of table Variables
   *
   * @param id       Database column id SqlType(INTEGER), AutoInc, PrimaryKey
   * @param name     Database column name SqlType(TEXT)
   * @param chatType Database column chat_type SqlType(TEXT)
   * @param chatId   Database column chat_id SqlType(INTEGER)
   * @param value    Database column value SqlType(TEXT), Default() */
  case class VariablesRow(id: Long, name: String, chatType: String, chatId: Long, value: String = "")

  /** GetResult implicit for fetching VariablesRow objects using plain SQL queries */
  implicit def GetResultVariablesRow(implicit e0: GR[Long], e1: GR[String]): GR[VariablesRow] = GR {
    prs =>
      import prs._
      VariablesRow.tupled((<<[Long], <<[String], <<[String], <<[Long], <<[String]))
  }

  /** Table description of table variables. Objects of this class serve as prototypes for rows in queries. */
  class Variables(_tableTag: Tag) extends profile.api.Table[VariablesRow](_tableTag, "variables") {
    def * = (id, name, chatType, chatId, value) <> (VariablesRow.tupled, VariablesRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name), Rep.Some(chatType), Rep.Some(chatId), Rep.Some(value))).shaped.<>({ r => import r._; _1.map(_ => VariablesRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INTEGER), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(TEXT) */
    val name: Rep[String] = column[String]("name")
    /** Database column chat_type SqlType(TEXT) */
    val chatType: Rep[String] = column[String]("chat_type")
    /** Database column chat_id SqlType(INTEGER) */
    val chatId: Rep[Long] = column[Long]("chat_id")
    /** Database column value SqlType(TEXT), Default() */
    val value: Rep[String] = column[String]("value", O.Default(""))
  }

  /** Collection-like TableQuery object for table Variables */
  lazy val Variables = new TableQuery(tag => new Variables(tag))
}
