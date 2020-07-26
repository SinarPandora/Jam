package o.lartifa.jam.database.temporary.schema

// AUTO-GENERATED Slick data model for table WebPictures
trait WebPicturesTable {

  self:Tables  =>

  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table WebPictures
   *  @param pid Database column pid SqlType(int8), PrimaryKey, Default(-1)
   *  @param uid Database column uid SqlType(int8), Default(-1)
   *  @param title Database column title SqlType(text), Default(无标题)
   *  @param author Database column author SqlType(text), Default(未知作者)
   *  @param url Database column url SqlType(text), Default()
   *  @param isR18 Database column is_r18 SqlType(bool), Default(false)
   *  @param width Database column width SqlType(int4), Default(-1)
   *  @param height Database column height SqlType(int4), Default(-1)
   *  @param tags Database column tags SqlType(text), Default()
   *  @param base64Data Database column base64_data SqlType(text), Default(None) */
  case class WebPicturesRow(pid: Long = -1L, uid: Long = -1L, title: String = "无标题", author: String = "未知作者", url: String = "", isR18: Boolean = false, width: Int = -1, height: Int = -1, tags: String = "", base64Data: Option[String] = None)
  /** GetResult implicit for fetching WebPicturesRow objects using plain SQL queries */
  implicit def GetResultWebPicturesRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Boolean], e3: GR[Int], e4: GR[Option[String]]): GR[WebPicturesRow] = GR{
    prs => import prs._
    WebPicturesRow.tupled((<<[Long], <<[Long], <<[String], <<[String], <<[String], <<[Boolean], <<[Int], <<[Int], <<[String], <<?[String]))
  }
  /** Table description of table web_pictures. Objects of this class serve as prototypes for rows in queries. */
  class WebPictures(_tableTag: Tag) extends profile.api.Table[WebPicturesRow](_tableTag, "web_pictures") {
    def * = (pid, uid, title, author, url, isR18, width, height, tags, base64Data) <> (WebPicturesRow.tupled, WebPicturesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(pid), Rep.Some(uid), Rep.Some(title), Rep.Some(author), Rep.Some(url), Rep.Some(isR18), Rep.Some(width), Rep.Some(height), Rep.Some(tags), base64Data).shaped.<>({ r=>import r._; _1.map(_=> WebPicturesRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column pid SqlType(int8), PrimaryKey, Default(-1) */
    val pid: Rep[Long] = column[Long]("pid", O.PrimaryKey, O.Default(-1L))
    /** Database column uid SqlType(int8), Default(-1) */
    val uid: Rep[Long] = column[Long]("uid", O.Default(-1L))
    /** Database column title SqlType(text), Default(无标题) */
    val title: Rep[String] = column[String]("title", O.Default("无标题"))
    /** Database column author SqlType(text), Default(未知作者) */
    val author: Rep[String] = column[String]("author", O.Default("未知作者"))
    /** Database column url SqlType(text), Default() */
    val url: Rep[String] = column[String]("url", O.Default(""))
    /** Database column is_r18 SqlType(bool), Default(false) */
    val isR18: Rep[Boolean] = column[Boolean]("is_r18", O.Default(false))
    /** Database column width SqlType(int4), Default(-1) */
    val width: Rep[Int] = column[Int]("width", O.Default(-1))
    /** Database column height SqlType(int4), Default(-1) */
    val height: Rep[Int] = column[Int]("height", O.Default(-1))
    /** Database column tags SqlType(text), Default() */
    val tags: Rep[String] = column[String]("tags", O.Default(""))
    /** Database column base64_data SqlType(text), Default(None) */
    val base64Data: Rep[Option[String]] = column[Option[String]]("base64_data", O.Default(None))
  }
  /** Collection-like TableQuery object for table WebPictures */
  lazy val WebPictures = new TableQuery(tag => new WebPictures(tag))
}
