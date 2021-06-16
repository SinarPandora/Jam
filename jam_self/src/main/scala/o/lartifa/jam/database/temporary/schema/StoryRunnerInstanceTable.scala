package o.lartifa.jam.database.temporary.schema

/**
 * Author: sinar
 * 2021/6/16 22:52
 */
// AUTO-GENERATED Slick data model for table StoryRunnerInstance
trait StoryRunnerInstanceTable {

  self: Tables =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** Entity class storing rows of table StoryRunnerInstance
   *
   * @param id         Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param storyId    Database column story_id SqlType(int8)
   * @param data       Database column data SqlType(varchar)
   * @param config     Database column config SqlType(varchar)
   * @param chatType   Database column chat_type SqlType(varchar), Length(15,true)
   * @param chatId     Database column chat_id SqlType(int8)
   * @param lastUpdate Database column last_update SqlType(timestamp) */
  case class StoryRunnerInstanceRow(id: Long, storyId: Long, data: String, config: String, chatType: String, chatId: Long, lastUpdate: java.sql.Timestamp)

  /** GetResult implicit for fetching StoryRunnerInstanceRow objects using plain SQL queries */
  implicit def GetResultStoryRunnerInstanceRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[StoryRunnerInstanceRow] = GR {
    prs =>
      import prs._
      StoryRunnerInstanceRow.tupled((<<[Long], <<[Long], <<[String], <<[String], <<[String], <<[Long], <<[java.sql.Timestamp]))
  }

  /** Table description of table story_runner_instance. Objects of this class serve as prototypes for rows in queries. */
  class StoryRunnerInstance(_tableTag: Tag) extends profile.api.Table[StoryRunnerInstanceRow](_tableTag, "story_runner_instance") {
    def * = (id, storyId, data, config, chatType, chatId, lastUpdate) <> (StoryRunnerInstanceRow.tupled, StoryRunnerInstanceRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(storyId), Rep.Some(data), Rep.Some(config), Rep.Some(chatType), Rep.Some(chatId), Rep.Some(lastUpdate)).shaped.<>({ r => import r._; _1.map(_ => StoryRunnerInstanceRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column story_id SqlType(int8) */
    val storyId: Rep[Long] = column[Long]("story_id")
    /** Database column data SqlType(varchar) */
    val data: Rep[String] = column[String]("data")
    /** Database column config SqlType(varchar) */
    val config: Rep[String] = column[String]("config")
    /** Database column chat_type SqlType(varchar), Length(15,true) */
    val chatType: Rep[String] = column[String]("chat_type", O.Length(15, varying = true))
    /** Database column chat_id SqlType(int8) */
    val chatId: Rep[Long] = column[Long]("chat_id")
    /** Database column last_update SqlType(timestamp) */
    val lastUpdate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("last_update")

    /** Foreign key referencing Story (database name story_instance_story_id_fk) */
    lazy val storyFk = foreignKey("story_instance_story_id_fk", storyId, Story)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  /** Collection-like TableQuery object for table StoryRunnerInstance */
  lazy val StoryRunnerInstance = new TableQuery(tag => new StoryRunnerInstance(tag))
}
