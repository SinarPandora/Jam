package o.lartifa.jam.database.schema

/**
 * Author: sinar
 * 2021/12/20 21:41
 */
// AUTO-GENERATED Slick data model for table BwyGroup
trait BwyGroupTable {

  self: Tables =>

  import profile.api.*
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR

  /** Entity class storing rows of table BwyGroup
   *
   * @param id         Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param name       Database column name SqlType(text)
   * @param detail     Database column detail SqlType(text), Default()
   * @param createTime Database column create_time SqlType(timestamp)
   * @param creatorQid Database column creator_qid SqlType(int8)
   * @param isActive   Database column is_active SqlType(bool), Default(true) */
  case class BwyGroupRow(id: Long, name: String, detail: String = "", createTime: java.sql.Timestamp, creatorQid: Long, isActive: Boolean = true)

  /** GetResult implicit for fetching BwyGroupRow objects using plain SQL queries */
  implicit def GetResultBwyGroupRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp], e3: GR[Boolean]): GR[BwyGroupRow] = GR {
    prs =>
      import prs.*
      BwyGroupRow.tupled((<<[Long], <<[String], <<[String], <<[java.sql.Timestamp], <<[Long], <<[Boolean]))
  }

  /** Table description of table bwy_group. Objects of this class serve as prototypes for rows in queries. */
  class BwyGroup(_tableTag: Tag) extends profile.api.Table[BwyGroupRow](_tableTag, "bwy_group") {
    def * = (id, name, detail, createTime, creatorQid, isActive) <> (BwyGroupRow.tupled, BwyGroupRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(detail), Rep.Some(createTime), Rep.Some(creatorQid), Rep.Some(isActive)).shaped.<>({ r => import r.*; _1.map(_ => BwyGroupRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column detail SqlType(text), Default() */
    val detail: Rep[String] = column[String]("detail", O.Default(""))
    /** Database column create_time SqlType(timestamp) */
    val createTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_time")
    /** Database column creator_qid SqlType(int8) */
    val creatorQid: Rep[Long] = column[Long]("creator_qid")
    /** Database column is_active SqlType(bool), Default(true) */
    val isActive: Rep[Boolean] = column[Boolean]("is_active", O.Default(true))
  }
  /** Collection-like TableQuery object for table BwyGroup */
  lazy val BwyGroup = new TableQuery(tag => new BwyGroup(tag))
}
