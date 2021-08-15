package o.lartifa.jam.database.temporary.schema

/**
 * Author: sinar
 * 2021/8/15 21:23
 */
// AUTO-GENERATED Slick data model for table TrpgGame
trait TrpgGameTable {

  self: Tables =>

  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** Entity class storing rows of table TrpgGame
   *
   * @param id       Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param name     Database column name SqlType(text)
   * @param ruleName Database column rule_name SqlType(text)
   * @param kpList   Database column kp_list SqlType(text) */
  case class TrpgGameRow(id: Long, name: String, ruleName: String, kpList: String)

  /** GetResult implicit for fetching TrpgGameRow objects using plain SQL queries */
  implicit def GetResultTrpgGameRow(implicit e0: GR[Long], e1: GR[String]): GR[TrpgGameRow] = GR {
    prs =>
      import prs._
      TrpgGameRow.tupled((<<[Long], <<[String], <<[String], <<[String]))
  }

  /** Table description of table trpg_game. Objects of this class serve as prototypes for rows in queries. */
  class TrpgGame(_tableTag: Tag) extends profile.api.Table[TrpgGameRow](_tableTag, "trpg_game") {
    def * = (id, name, ruleName, kpList) <> (TrpgGameRow.tupled, TrpgGameRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(ruleName), Rep.Some(kpList)).shaped.<>({ r => import r._; _1.map(_ => TrpgGameRow.tupled((_1.get, _2.get, _3.get, _4.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column rule_name SqlType(text) */
    val ruleName: Rep[String] = column[String]("rule_name")
    /** Database column kp_list SqlType(text) */
    val kpList: Rep[String] = column[String]("kp_list")
  }

  /** Collection-like TableQuery object for table TrpgGame */
  lazy val TrpgGame = new TableQuery(tag => new TrpgGame(tag))
}
