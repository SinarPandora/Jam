package o.lartifa.jam.database.schema

// AUTO-GENERATED Slick data model for table StorySaveInherit
trait StorySaveInheritTable {

  self:Tables  =>

  import profile.api.*
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR
  /** Entity class storing rows of table StorySaveInherit
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param legacyStoryId Database column legacy_story_id SqlType(int8)
   *  @param inUseStoryId Database column in_use_story_id SqlType(int8) */
  case class StorySaveInheritRow(id: Long, legacyStoryId: Long, inUseStoryId: Long)
  /** GetResult implicit for fetching StorySaveInheritRow objects using plain SQL queries */
  implicit def GetResultStorySaveInheritRow(implicit e0: GR[Long]): GR[StorySaveInheritRow] = GR{
    prs => import prs.*
    StorySaveInheritRow.tupled((<<[Long], <<[Long], <<[Long]))
  }
  /** Table description of table story_save_inherit. Objects of this class serve as prototypes for rows in queries. */
  class StorySaveInherit(_tableTag: Tag) extends profile.api.Table[StorySaveInheritRow](_tableTag, "story_save_inherit") {
    def * = (id, legacyStoryId, inUseStoryId) <> (StorySaveInheritRow.tupled, StorySaveInheritRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(legacyStoryId), Rep.Some(inUseStoryId)).shaped.<>({ r=>import r.*; _1.map(_=> StorySaveInheritRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column legacy_story_id SqlType(int8) */
    val legacyStoryId: Rep[Long] = column[Long]("legacy_story_id")
    /** Database column in_use_story_id SqlType(int8) */
    val inUseStoryId: Rep[Long] = column[Long]("in_use_story_id")

    /** Foreign key referencing Story (database name story_save_inherit_in_use_story_id_fk) */
    lazy val storyFk1 = foreignKey("story_save_inherit_in_use_story_id_fk", inUseStoryId, Story)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing Story (database name story_save_inherit_legacy_story_id_fk) */
    lazy val storyFk2 = foreignKey("story_save_inherit_legacy_story_id_fk", legacyStoryId, Story)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table StorySaveInherit */
  lazy val StorySaveInherit = new TableQuery(tag => new StorySaveInherit(tag))
}
