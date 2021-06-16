package o.lartifa.jam.database.temporary.schema

// AUTO-GENERATED Slick data model for table StorySaveFile
trait StorySaveFileTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table StorySaveFile
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param storyId Database column story_id SqlType(int8)
   *  @param data Database column data SqlType(varchar)
   *  @param config Database column config SqlType(varchar)
   *  @param chatType Database column chat_type SqlType(varchar), Length(15,true)
   *  @param chatId Database column chat_id SqlType(int8)
   *  @param recordTime Database column record_time SqlType(timestamp)
   *  @param isAutoSaved Database column is_auto_saved SqlType(bool), Default(false)
   *  @param name Database column name SqlType(varchar), Default(None) */
  case class StorySaveFileRow(id: Long, storyId: Long, data: String, config: String, chatType: String, chatId: Long, recordTime: java.sql.Timestamp, isAutoSaved: Boolean = false, name: Option[String] = None)
  /** GetResult implicit for fetching StorySaveFileRow objects using plain SQL queries */
  implicit def GetResultStorySaveFileRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp], e3: GR[Boolean], e4: GR[Option[String]]): GR[StorySaveFileRow] = GR{
    prs => import prs._
      StorySaveFileRow.tupled((<<[Long], <<[Long], <<[String], <<[String], <<[String], <<[Long], <<[java.sql.Timestamp], <<[Boolean], <<?[String]))
  }
  /** Table description of table story_save_file. Objects of this class serve as prototypes for rows in queries. */
  class StorySaveFile(_tableTag: Tag) extends profile.api.Table[StorySaveFileRow](_tableTag, "story_save_file") {
    def * = (id, storyId, data, config, chatType, chatId, recordTime, isAutoSaved, name) <> (StorySaveFileRow.tupled, StorySaveFileRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(storyId), Rep.Some(data), Rep.Some(config), Rep.Some(chatType), Rep.Some(chatId), Rep.Some(recordTime), Rep.Some(isAutoSaved), name).shaped.<>({ r=>import r._; _1.map(_=> StorySaveFileRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column story_id SqlType(int8) */
    val storyId: Rep[Long] = column[Long]("story_id")
    /** Database column data SqlType(varchar) */
    val data: Rep[String] = column[String]("data")
    /** Database column config SqlType(varchar) */
    val config: Rep[String] = column[String]("config")
    /** Database column chat_type SqlType(varchar), Length(15,true) */
    val chatType: Rep[String] = column[String]("chat_type", O.Length(15,varying=true))
    /** Database column chat_id SqlType(int8) */
    val chatId: Rep[Long] = column[Long]("chat_id")
    /** Database column record_time SqlType(timestamp) */
    val recordTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("record_time")
    /** Database column is_auto_saved SqlType(bool), Default(false) */
    val isAutoSaved: Rep[Boolean] = column[Boolean]("is_auto_saved", O.Default(false))
    /** Database column name SqlType(varchar), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Default(None))

    /** Foreign key referencing Story (database name story_save_file_story_id_fk) */
    lazy val storyFk = foreignKey("story_save_file_story_id_fk", storyId, Story)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)

    /** Index over (chatId,chatType,storyId) (database name story_save_file_chat_id_chat_type_story_id_uindex) */
    val index1 = index("story_save_file_chat_id_chat_type_story_id_uindex", (chatId, chatType, storyId))
  }
  /** Collection-like TableQuery object for table StorySaveFile */
  lazy val StorySaveFile = new TableQuery(tag => new StorySaveFile(tag))
}
