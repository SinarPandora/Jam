package o.lartifa.jam.database.temporary.schema

// AUTO-GENERATED Slick data model for table Story
trait StoryTable {

  self:Tables  =>

  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Story
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param path Database column path SqlType(varchar)
   *  @param name Database column name SqlType(varchar)
   *  @param checksum Database column checksum SqlType(varchar)
   *  @param keyword Database column keyword SqlType(varchar), Default()
   *  @param author Database column author SqlType(varchar), Default(无名氏)
   *  @param script Database column script SqlType(varchar)
   *  @param status Database column status SqlType(varchar), Length(10,true), Default(使用中)
   *  @param loadDate Database column load_date SqlType(timestamp)
   *  @param defaultConfig Database column default_config SqlType(varchar) */
  case class StoryRow(id: Long, path: String, name: String, checksum: String, keyword: String = "", author: String = "无名氏", script: String, status: String = "使用中", loadDate: java.sql.Timestamp, defaultConfig: String)
  /** GetResult implicit for fetching StoryRow objects using plain SQL queries */
  implicit def GetResultStoryRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[StoryRow] = GR{
    prs => import prs._
    StoryRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[java.sql.Timestamp], <<[String]))
  }
  /** Table description of table story. Objects of this class serve as prototypes for rows in queries. */
  class Story(_tableTag: Tag) extends profile.api.Table[StoryRow](_tableTag, "story") {
    def * = (id, path, name, checksum, keyword, author, script, status, loadDate, defaultConfig) <> (StoryRow.tupled, StoryRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(path), Rep.Some(name), Rep.Some(checksum), Rep.Some(keyword), Rep.Some(author), Rep.Some(script), Rep.Some(status), Rep.Some(loadDate), Rep.Some(defaultConfig)).shaped.<>({ r=>import r._; _1.map(_=> StoryRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column path SqlType(varchar) */
    val path: Rep[String] = column[String]("path")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column checksum SqlType(varchar) */
    val checksum: Rep[String] = column[String]("checksum")
    /** Database column keyword SqlType(varchar), Default() */
    val keyword: Rep[String] = column[String]("keyword", O.Default(""))
    /** Database column author SqlType(varchar), Default(无名氏) */
    val author: Rep[String] = column[String]("author", O.Default("无名氏"))
    /** Database column script SqlType(varchar) */
    val script: Rep[String] = column[String]("script")
    /** Database column status SqlType(varchar), Length(10,true), Default(使用中) */
    val status: Rep[String] = column[String]("status", O.Length(10,varying=true), O.Default("使用中"))
    /** Database column load_date SqlType(timestamp) */
    val loadDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("load_date")
    /** Database column default_config SqlType(varchar) */
    val defaultConfig: Rep[String] = column[String]("default_config")

    /** Index over (author,name) (database name story_author_name_index) */
    val index1 = index("story_author_name_index", (author, name))
  }
  /** Collection-like TableQuery object for table Story */
  lazy val Story = new TableQuery(tag => new Story(tag))
}
