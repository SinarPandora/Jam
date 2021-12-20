package o.lartifa.jam.database.schema

/**
 * Author: sinar
 * 2021/12/20 21:41
 */
// AUTO-GENERATED Slick data model for table BwyGroupMember
trait BwyGroupMemberTable {

  self: Tables =>

  import profile.api.*
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR

  /** Entity class storing rows of table BwyGroupMember
   *
   * @param id         Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param groupId    Database column group_id SqlType(int8)
   * @param memberQid  Database column member_qid SqlType(int8)
   * @param createTime Database column create_time SqlType(timestamp) */
  case class BwyGroupMemberRow(id: Long, groupId: Long, memberQid: Long, createTime: java.sql.Timestamp)

  /** GetResult implicit for fetching BwyGroupMemberRow objects using plain SQL queries */
  implicit def GetResultBwyGroupMemberRow(implicit e0: GR[Long], e1: GR[java.sql.Timestamp]): GR[BwyGroupMemberRow] = GR {
    prs =>
      import prs.*
      BwyGroupMemberRow.tupled((<<[Long], <<[Long], <<[Long], <<[java.sql.Timestamp]))
  }

  /** Table description of table bwy_group_member. Objects of this class serve as prototypes for rows in queries. */
  class BwyGroupMember(_tableTag: Tag) extends profile.api.Table[BwyGroupMemberRow](_tableTag, "bwy_group_member") {
    def * = (id, groupId, memberQid, createTime) <> (BwyGroupMemberRow.tupled, BwyGroupMemberRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(groupId), Rep.Some(memberQid), Rep.Some(createTime)).shaped.<>({ r => import r.*; _1.map(_ => BwyGroupMemberRow.tupled((_1.get, _2.get, _3.get, _4.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column group_id SqlType(int8) */
    val groupId: Rep[Long] = column[Long]("group_id")
    /** Database column member_qid SqlType(int8) */
    val memberQid: Rep[Long] = column[Long]("member_qid")
    /** Database column create_time SqlType(timestamp) */
    val createTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_time")

    /** Foreign key referencing BwyGroup (database name bwy_group_member_group_id_fkey) */
    lazy val bwyGroupFk = foreignKey("bwy_group_member_group_id_fkey", groupId, BwyGroup)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BwyGroupMember */
  lazy val BwyGroupMember = new TableQuery(tag => new BwyGroupMember(tag))
}
