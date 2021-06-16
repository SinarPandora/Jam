package o.lartifa.jam.database.temporary.schema

/**
 * Author: sinar
 * 2021/6/16 22:52
 */
// AUTO-GENERATED Slick data model for table StoryRunnerConfig
trait StoryRunnerConfigTable {

  self: Tables =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** Entity class storing rows of table StoryRunnerConfig
   *
   * @param id         Database column id SqlType(bigserial), AutoInc, PrimaryKey
   * @param storyId    Database column story_id SqlType(int8)
   * @param chatType   Database column chat_type SqlType(varchar), Length(15,true)
   * @param chatId     Database column chat_id SqlType(int8)
   * @param config     Database column config SqlType(varchar)
   * @param lastUpdate Database column last_update SqlType(timestamp) */
  case class StoryRunnerConfigRow(id: Long, storyId: Long, chatType: String, chatId: Long, config: String, lastUpdate: java.sql.Timestamp)

  /** GetResult implicit for fetching StoryRunnerConfigRow objects using plain SQL queries */
  implicit def GetResultStoryRunnerConfigRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[StoryRunnerConfigRow] = GR {
    prs =>
      import prs._
      StoryRunnerConfigRow.tupled((<<[Long], <<[Long], <<[String], <<[Long], <<[String], <<[java.sql.Timestamp]))
  }

  /** Table description of table story_runner_config. Objects of this class serve as prototypes for rows in queries. */
  class StoryRunnerConfig(_tableTag: Tag) extends profile.api.Table[StoryRunnerConfigRow](_tableTag, "story_runner_config") {
    def * = (id, storyId, chatType, chatId, config, lastUpdate) <> (StoryRunnerConfigRow.tupled, StoryRunnerConfigRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(storyId), Rep.Some(chatType), Rep.Some(chatId), Rep.Some(config), Rep.Some(lastUpdate)).shaped.<>({ r => import r._; _1.map(_ => StoryRunnerConfigRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column story_id SqlType(int8) */
    val storyId: Rep[Long] = column[Long]("story_id")
    /** Database column chat_type SqlType(varchar), Length(15,true) */
    val chatType: Rep[String] = column[String]("chat_type", O.Length(15, varying = true))
    /** Database column chat_id SqlType(int8) */
    val chatId: Rep[Long] = column[Long]("chat_id")
    /** Database column config SqlType(varchar) */
    val config: Rep[String] = column[String]("config")
    /** Database column last_update SqlType(timestamp) */
    val lastUpdate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("last_update")

    /** Foreign key referencing Story (database name story_runner_config_story_id_fk) */
    lazy val storyFk = foreignKey("story_runner_config_story_id_fk", storyId, Story)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  /** Collection-like TableQuery object for table StoryRunnerConfig */
  lazy val StoryRunnerConfig = new TableQuery(tag => new StoryRunnerConfig(tag))
}
