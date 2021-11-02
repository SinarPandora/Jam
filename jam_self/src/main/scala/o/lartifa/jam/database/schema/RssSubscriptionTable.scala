package o.lartifa.jam.database.schema

// AUTO-GENERATED Slick data model for table RssSubscription
trait RssSubscriptionTable {

  self:Tables  =>

  import profile.api.*
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.GetResult as GR
  /** Entity class storing rows of table RssSubscription
   *  @param source Database column source SqlType(text), PrimaryKey
   *  @param sourceCategory Database column source_category SqlType(text)
   *  @param subscribers Database column subscribers SqlType(text)
   *  @param channel Database column channel SqlType(text)
   *  @param lastKey Database column last_key SqlType(text), Default(IS_NOT_A_KEY)
   *  @param lastUpdate Database column last_update SqlType(timestamp) */
  case class RssSubscriptionRow(source: String, sourceCategory: String, subscribers: String, channel: String, lastKey: String = "IS_NOT_A_KEY", lastUpdate: java.sql.Timestamp)
  /** GetResult implicit for fetching RssSubscriptionRow objects using plain SQL queries */
  implicit def GetResultRssSubscriptionRow(implicit e0: GR[String], e1: GR[java.sql.Timestamp]): GR[RssSubscriptionRow] = GR{
    prs => import prs.*
      RssSubscriptionRow.tupled((<<[String], <<[String], <<[String], <<[String], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table rss_subscription. Objects of this class serve as prototypes for rows in queries. */
  class RssSubscription(_tableTag: Tag) extends profile.api.Table[RssSubscriptionRow](_tableTag, "rss_subscription") {
    def * = (source, sourceCategory, subscribers, channel, lastKey, lastUpdate) <> (RssSubscriptionRow.tupled, RssSubscriptionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(source), Rep.Some(sourceCategory), Rep.Some(subscribers), Rep.Some(channel), Rep.Some(lastKey), Rep.Some(lastUpdate))).shaped.<>({r=>import r.*; _1.map(_=> RssSubscriptionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column source SqlType(text), PrimaryKey */
    val source: Rep[String] = column[String]("source", O.PrimaryKey)
    /** Database column source_category SqlType(text) */
    val sourceCategory: Rep[String] = column[String]("source_category")
    /** Database column subscribers SqlType(text) */
    val subscribers: Rep[String] = column[String]("subscribers")
    /** Database column channel SqlType(text) */
    val channel: Rep[String] = column[String]("channel")
    /** Database column last_key SqlType(text), Default(IS_NOT_A_KEY) */
    val lastKey: Rep[String] = column[String]("last_key", O.Default("IS_NOT_A_KEY"))
    /** Database column last_update SqlType(timestamp) */
    val lastUpdate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("last_update")
  }
  /** Collection-like TableQuery object for table RssSubscription */
  lazy val RssSubscription = new TableQuery(tag => new RssSubscription(tag))
}
