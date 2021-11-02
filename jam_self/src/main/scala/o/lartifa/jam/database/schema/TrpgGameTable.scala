package o.lartifa.jam.database.schema

// AUTO-GENERATED Slick data model for table TrpgGame
trait TrpgGameTable {

  self:Tables  =>

  import profile.api.*
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR
  /** Entity class storing rows of table TrpgGame
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(text)
   *  @param ruleName Database column rule_name SqlType(text)
   *  @param kpList Database column kp_list SqlType(text)
   *  @param lastChat Database column last_chat SqlType(text), Default(None) */
  case class TrpgGameRow(id: Long, name: String, ruleName: String, kpList: String, lastChat: Option[String] = None)
  /** GetResult implicit for fetching TrpgGameRow objects using plain SQL queries */
  implicit def GetResultTrpgGameRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]]): GR[TrpgGameRow] = GR{
    prs => import prs.*
      TrpgGameRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<?[String]))
  }
  /** Table description of table trpg_game. Objects of this class serve as prototypes for rows in queries. */
  class TrpgGame(_tableTag: Tag) extends profile.api.Table[TrpgGameRow](_tableTag, "trpg_game") {
    def * = (id, name, ruleName, kpList, lastChat) <> (TrpgGameRow.tupled, TrpgGameRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(ruleName), Rep.Some(kpList), lastChat).shaped.<>({ r=>import r.*; _1.map(_=> TrpgGameRow.tupled((_1.get, _2.get, _3.get, _4.get, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column rule_name SqlType(text) */
    val ruleName: Rep[String] = column[String]("rule_name")
    /** Database column kp_list SqlType(text) */
    val kpList: Rep[String] = column[String]("kp_list")
    /** Database column last_chat SqlType(text), Default(None) */
    val lastChat: Rep[Option[String]] = column[Option[String]]("last_chat", O.Default(None))
  }
  /** Collection-like TableQuery object for table TrpgGame */
  lazy val TrpgGame = new TableQuery(tag => new TrpgGame(tag))
}
