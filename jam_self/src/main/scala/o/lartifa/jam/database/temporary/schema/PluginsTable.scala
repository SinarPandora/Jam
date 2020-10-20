package o.lartifa.jam.database.temporary.schema

// AUTO-GENERATED Slick data model for table Plugins
trait PluginsTable {

  self:Tables  =>

  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Plugins
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(varchar)
   *  @param keywords Database column keywords SqlType(varchar), Default()
   *  @param author Database column author SqlType(varchar)
   *  @param `package` Database column package SqlType(varchar)
   *  @param installDate Database column install_date SqlType(timestamp)
   *  @param isEnabled Database column is_enabled SqlType(bool), Default(false)
   *  @param version Database column version SqlType(numeric), Default(0.1) */
  case class PluginsRow(id: Long, name: String, keywords: String = "", author: String, `package`: String, installDate: java.sql.Timestamp, isEnabled: Boolean = false, version: scala.math.BigDecimal = scala.math.BigDecimal("0.1"))
  /** GetResult implicit for fetching PluginsRow objects using plain SQL queries */
  implicit def GetResultPluginsRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp], e3: GR[Boolean], e4: GR[scala.math.BigDecimal]): GR[PluginsRow] = GR{
    prs => import prs._
      PluginsRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[java.sql.Timestamp], <<[Boolean], <<[scala.math.BigDecimal]))
  }
  /** Table description of table plugins. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: package */
  class Plugins(_tableTag: Tag) extends profile.api.Table[PluginsRow](_tableTag, "plugins") {
    def * = (id, name, keywords, author, `package`, installDate, isEnabled, version) <> (PluginsRow.tupled, PluginsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(keywords), Rep.Some(author), Rep.Some(`package`), Rep.Some(installDate), Rep.Some(isEnabled), Rep.Some(version)).shaped.<>({ r=>import r._; _1.map(_=> PluginsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column keywords SqlType(varchar), Default() */
    val keywords: Rep[String] = column[String]("keywords", O.Default(""))
    /** Database column author SqlType(varchar) */
    val author: Rep[String] = column[String]("author")
    /** Database column package SqlType(varchar)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `package`: Rep[String] = column[String]("package")
    /** Database column install_date SqlType(timestamp) */
    val installDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("install_date")
    /** Database column is_enabled SqlType(bool), Default(false) */
    val isEnabled: Rep[Boolean] = column[Boolean]("is_enabled", O.Default(false))
    /** Database column version SqlType(numeric), Default(0.1) */
    val version: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("version", O.Default(scala.math.BigDecimal("0.1")))

    /** Uniqueness Index over (`package`) (database name plugins_package_uindex) */
    val index1 = index("plugins_package_uindex", `package`, unique=true)
  }
  /** Collection-like TableQuery object for table Plugins */
  lazy val Plugins = new TableQuery(tag => new Plugins(tag))
}
