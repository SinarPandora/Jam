package o.lartifa.jam.database.temporary.schema

/**
 * Author: sinar
 * 2021/8/16 00:46
 */
// AUTO-GENERATED Slick data model for table TrpgActor
trait TrpgActorTable {

  self: Tables =>

  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** Entity class storing rows of table TrpgActor
   *
   * @param id            Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param name          Database column name SqlType(text)
   * @param qid           Database column qid SqlType(int8)
   * @param attr          Database column attr SqlType(text)
   * @param info          Database column info SqlType(text)
   * @param defaultConfig Database column default_config SqlType(text), Default({})
   * @param isActive      Database column is_active SqlType(bool), Default(false) */
  case class TrpgActorRow(id: Long, name: String, qid: Long, attr: String, info: String, defaultConfig: String = "{}", isActive: Boolean = false)

  /** GetResult implicit for fetching TrpgActorRow objects using plain SQL queries */
  implicit def GetResultTrpgActorRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Boolean]): GR[TrpgActorRow] = GR {
    prs =>
      import prs._
      TrpgActorRow.tupled((<<[Long], <<[String], <<[Long], <<[String], <<[String], <<[String], <<[Boolean]))
  }

  /** Table description of table trpg_actor. Objects of this class serve as prototypes for rows in queries. */
  class TrpgActor(_tableTag: Tag) extends profile.api.Table[TrpgActorRow](_tableTag, "trpg_actor") {
    def * = (id, name, qid, attr, info, defaultConfig, isActive) <> (TrpgActorRow.tupled, TrpgActorRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(qid), Rep.Some(attr), Rep.Some(info), Rep.Some(defaultConfig), Rep.Some(isActive)).shaped.<>({ r => import r._; _1.map(_ => TrpgActorRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column qid SqlType(int8) */
    val qid: Rep[Long] = column[Long]("qid")
    /** Database column attr SqlType(text) */
    val attr: Rep[String] = column[String]("attr")
    /** Database column info SqlType(text) */
    val info: Rep[String] = column[String]("info")
    /** Database column default_config SqlType(text), Default({}) */
    val defaultConfig: Rep[String] = column[String]("default_config", O.Default("{}"))
    /** Database column is_active SqlType(bool), Default(false) */
    val isActive: Rep[Boolean] = column[Boolean]("is_active", O.Default(false))
  }

  /** Collection-like TableQuery object for table TrpgActor */
  lazy val TrpgActor = new TableQuery(tag => new TrpgActor(tag))
}
