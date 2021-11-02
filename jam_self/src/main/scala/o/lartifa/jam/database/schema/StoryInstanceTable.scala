package o.lartifa.jam.database.schema

// AUTO-GENERATED Slick data model for table StoryInstance
trait StoryInstanceTable {

  self:Tables  =>

  import profile.api.*
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR
  /** Entity class storing rows of table StoryInstance
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param storyId Database column story_id SqlType(int8)
   *  @param data Database column data SqlType(varchar), Default(None)
   *  @param autoSave Database column auto_save SqlType(varchar), Default()
   *  @param config Database column config SqlType(varchar)
   *  @param chatType Database column chat_type SqlType(varchar), Length(15,true)
   *  @param chatId Database column chat_id SqlType(int8)
   *  @param lastUpdate Database column last_update SqlType(timestamp) */
  case class StoryInstanceRow(id: Long, storyId: Long, data: Option[String] = None, autoSave: String = "", config: String, chatType: String, chatId: Long, lastUpdate: java.sql.Timestamp)
  /** GetResult implicit for fetching StoryInstanceRow objects using plain SQL queries */
  implicit def GetResultStoryInstanceRow(implicit e0: GR[Long], e1: GR[Option[String]], e2: GR[String], e3: GR[java.sql.Timestamp]): GR[StoryInstanceRow] = GR{
    prs => import prs.*
    StoryInstanceRow.tupled((<<[Long], <<[Long], <<?[String], <<[String], <<[String], <<[String], <<[Long], <<[java.sql.Timestamp]))
  }
  /** Table description of table story_instance. Objects of this class serve as prototypes for rows in queries. */
  class StoryInstance(_tableTag: Tag) extends profile.api.Table[StoryInstanceRow](_tableTag, "story_instance") {
    def * = (id, storyId, data, autoSave, config, chatType, chatId, lastUpdate) <> (StoryInstanceRow.tupled, StoryInstanceRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(storyId), data, Rep.Some(autoSave), Rep.Some(config), Rep.Some(chatType), Rep.Some(chatId), Rep.Some(lastUpdate)).shaped.<>({ r=>import r.*; _1.map(_=> StoryInstanceRow.tupled((_1.get, _2.get, _3, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column story_id SqlType(int8) */
    val storyId: Rep[Long] = column[Long]("story_id")
    /** Database column data SqlType(varchar), Default(None) */
    val data: Rep[Option[String]] = column[Option[String]]("data", O.Default(None))
    /** Database column auto_save SqlType(varchar), Default() */
    val autoSave: Rep[String] = column[String]("auto_save", O.Default(""))
    /** Database column config SqlType(varchar) */
    val config: Rep[String] = column[String]("config")
    /** Database column chat_type SqlType(varchar), Length(15,true) */
    val chatType: Rep[String] = column[String]("chat_type", O.Length(15,varying=true))
    /** Database column chat_id SqlType(int8) */
    val chatId: Rep[Long] = column[Long]("chat_id")
    /** Database column last_update SqlType(timestamp) */
    val lastUpdate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("last_update")

    /** Foreign key referencing Story (database name story_instance_story_id_fk) */
    lazy val storyFk = foreignKey("story_instance_story_id_fk", storyId, Story)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table StoryInstance */
  lazy val StoryInstance = new TableQuery(tag => new StoryInstance(tag))
}
