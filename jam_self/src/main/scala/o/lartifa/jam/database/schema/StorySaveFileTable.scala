package o.lartifa.jam.database.schema

// AUTO-GENERATED Slick data model for table StorySaveFile
trait StorySaveFileTable {

  self:Tables  =>

  import profile.api.*
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR
  /** Entity class storing rows of table StorySaveFile
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param storyId Database column story_id SqlType(int8)
   *  @param chatId Database column chat_id SqlType(int8)
   *  @param saveList Database column save_list SqlType(varchar), Default()
   *  @param chatType Database column chat_type SqlType(varchar), Length(15,true)
   *  @param chatScopeDefaultConfig Database column chat_scope_default_config SqlType(varchar) */
  case class StorySaveFileRow(id: Long, storyId: Long, chatId: Long, saveList: String = "", chatType: String, chatScopeDefaultConfig: String)
  /** GetResult implicit for fetching StorySaveFileRow objects using plain SQL queries */
  implicit def GetResultStorySaveFileRow(implicit e0: GR[Long], e1: GR[String]): GR[StorySaveFileRow] = GR{
    prs => import prs.*
    StorySaveFileRow.tupled((<<[Long], <<[Long], <<[Long], <<[String], <<[String], <<[String]))
  }
  /** Table description of table story_save_file. Objects of this class serve as prototypes for rows in queries. */
  class StorySaveFile(_tableTag: Tag) extends profile.api.Table[StorySaveFileRow](_tableTag, "story_save_file") {
    def * = (id, storyId, chatId, saveList, chatType, chatScopeDefaultConfig) <> (StorySaveFileRow.tupled, StorySaveFileRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(storyId), Rep.Some(chatId), Rep.Some(saveList), Rep.Some(chatType), Rep.Some(chatScopeDefaultConfig)).shaped.<>({ r=>import r.*; _1.map(_=> StorySaveFileRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column story_id SqlType(int8) */
    val storyId: Rep[Long] = column[Long]("story_id")
    /** Database column chat_id SqlType(int8) */
    val chatId: Rep[Long] = column[Long]("chat_id")
    /** Database column save_list SqlType(varchar), Default() */
    val saveList: Rep[String] = column[String]("save_list", O.Default(""))
    /** Database column chat_type SqlType(varchar), Length(15,true) */
    val chatType: Rep[String] = column[String]("chat_type", O.Length(15,varying=true))
    /** Database column chat_scope_default_config SqlType(varchar) */
    val chatScopeDefaultConfig: Rep[String] = column[String]("chat_scope_default_config")

    /** Foreign key referencing Story (database name story_save_file_story_id_fk) */
    lazy val storyFk = foreignKey("story_save_file_story_id_fk", storyId, Story)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)

    /** Uniqueness Index over (chatId,chatType,storyId) (database name story_save_file_chat_id_chat_type_story_id_uindex) */
    val index1 = index("story_save_file_chat_id_chat_type_story_id_uindex", (chatId, chatType, storyId), unique=true)
  }
  /** Collection-like TableQuery object for table StorySaveFile */
  lazy val StorySaveFile = new TableQuery(tag => new StorySaveFile(tag))
}
