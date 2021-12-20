package o.lartifa.jam.database.schema

/**
 * Author: sinar
 * 2021/12/20 22:19
 */
// AUTO-GENERATED Slick data model for table BwyProject
trait BwyProjectTable {

  self: Tables =>

  import profile.api.*
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR

  /** Entity class storing rows of table BwyProject
   *
   * @param id         Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param name       Database column name SqlType(text)
   * @param detail     Database column detail SqlType(text), Default()
   * @param dueDate    Database column due_date SqlType(date)
   * @param createQid  Database column create_qid SqlType(int8)
   * @param createTime Database column create_time SqlType(timestamp)
   * @param isActive   Database column is_active SqlType(bool), Default(true) */
  case class BwyProjectRow(id: Long, name: String, detail: String = "", dueDate: java.sql.Date, createQid: Long, createTime: java.sql.Timestamp, isActive: Boolean = true)

  /** GetResult implicit for fetching BwyProjectRow objects using plain SQL queries */
  implicit def GetResultBwyProjectRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Date], e3: GR[java.sql.Timestamp], e4: GR[Boolean]): GR[BwyProjectRow] = GR {
    prs =>
      import prs.*
      BwyProjectRow.tupled((<<[Long], <<[String], <<[String], <<[java.sql.Date], <<[Long], <<[java.sql.Timestamp], <<[Boolean]))
  }

  /** Table description of table bwy_project. Objects of this class serve as prototypes for rows in queries. */
  class BwyProject(_tableTag: Tag) extends profile.api.Table[BwyProjectRow](_tableTag, "bwy_project") {
    def * = (id, name, detail, dueDate, createQid, createTime, isActive) <> (BwyProjectRow.tupled, BwyProjectRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(detail), Rep.Some(dueDate), Rep.Some(createQid), Rep.Some(createTime), Rep.Some(isActive)).shaped.<>({ r => import r.*; _1.map(_ => BwyProjectRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column detail SqlType(text), Default() */
    val detail: Rep[String] = column[String]("detail", O.Default(""))
    /** Database column due_date SqlType(date) */
    val dueDate: Rep[java.sql.Date] = column[java.sql.Date]("due_date")
    /** Database column create_qid SqlType(int8) */
    val createQid: Rep[Long] = column[Long]("create_qid")
    /** Database column create_time SqlType(timestamp) */
    val createTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_time")
    /** Database column is_active SqlType(bool), Default(true) */
    val isActive: Rep[Boolean] = column[Boolean]("is_active", O.Default(true))
  }
  /** Collection-like TableQuery object for table BwyProject */
  lazy val BwyProject = new TableQuery(tag => new BwyProject(tag))
}
