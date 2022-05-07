package o.lartifa.jam.database.schema

/**
 * Author: sinar
 * 2021/12/20 22:18
 */
// AUTO-GENERATED Slick data model for table BwyGroupProject
trait BwyGroupProjectTable {

  self: Tables =>

  import profile.api.*
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR

  /** Entity class storing rows of table BwyGroupProject
   *
   * @param id        Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param groupId   Database column group_id SqlType(int8)
   * @param projectId Database column project_id SqlType(int8) */
  case class BwyGroupProjectRow(id: Long, groupId: Long, projectId: Long)

  /** GetResult implicit for fetching BwyGroupProjectRow objects using plain SQL queries */
  implicit def GetResultBwyGroupProjectRow(implicit e0: GR[Long]): GR[BwyGroupProjectRow] = GR {
    prs =>
      import prs.*
      BwyGroupProjectRow.tupled((<<[Long], <<[Long], <<[Long]))
  }

  /** Table description of table bwy_group_project. Objects of this class serve as prototypes for rows in queries. */
  class BwyGroupProject(_tableTag: Tag) extends profile.api.Table[BwyGroupProjectRow](_tableTag, "bwy_group_project") {
    def * = (id, groupId, projectId) <> (BwyGroupProjectRow.tupled, BwyGroupProjectRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(groupId), Rep.Some(projectId)).shaped.<>({ r => import r.*; _1.map(_ => BwyGroupProjectRow.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column group_id SqlType(int8) */
    val groupId: Rep[Long] = column[Long]("group_id")
    /** Database column project_id SqlType(int8) */
    val projectId: Rep[Long] = column[Long]("project_id")

    /** Foreign key referencing BwyGroup (database name bwy_group_project_group_id_fkey) */
    lazy val bwyGroupFk = foreignKey("bwy_group_project_group_id_fkey", groupId, BwyGroup)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing BwyProject (database name bwy_group_project_project_id_fkey) */
    lazy val bwyProjectFk = foreignKey("bwy_group_project_project_id_fkey", projectId, BwyProject)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BwyGroupProject */
  lazy val BwyGroupProject = new TableQuery(tag => new BwyGroupProject(tag))
}
