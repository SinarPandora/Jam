package o.lartifa.jam.database.temporary.schema

// AUTO-GENERATED Slick data model for table RssSubscription
trait RssSubscriptionTable {

  self:Tables  =>

  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table RssSubscription
   *  @param sourceUrl Database column source_url SqlType(text), PrimaryKey
   *  @param sourceCategory Database column source_category SqlType(text)
   *  @param subscribers Database column subscribers SqlType(text)
   *  @param lastKey Database column last_key SqlType(text)
   *  @param lastUpdate Database column last_update SqlType(timestamp) */
  case class RssSubscriptionRow(sourceUrl: String, sourceCategory: String, subscribers: String, lastKey: String, lastUpdate: java.sql.Timestamp)
  /** GetResult implicit for fetching RssSubscriptionRow objects using plain SQL queries */
  implicit def GetResultRssSubscriptionRow(implicit e0: GR[String], e1: GR[java.sql.Timestamp]): GR[RssSubscriptionRow] = GR{
    prs => import prs._
    RssSubscriptionRow.tupled((<<[String], <<[String], <<[String], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table rss_subscription. Objects of this class serve as prototypes for rows in queries. */
  class RssSubscription(_tableTag: Tag) extends profile.api.Table[RssSubscriptionRow](_tableTag, "rss_subscription") {
    def * = (sourceUrl, sourceCategory, subscribers, lastKey, lastUpdate) <> (RssSubscriptionRow.tupled, RssSubscriptionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(sourceUrl), Rep.Some(sourceCategory), Rep.Some(subscribers), Rep.Some(lastKey), Rep.Some(lastUpdate))).shaped.<>({r=>import r._; _1.map(_=> RssSubscriptionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column source_url SqlType(text), PrimaryKey */
    val sourceUrl: Rep[String] = column[String]("source_url", O.PrimaryKey)
    /** Database column source_category SqlType(text) */
    val sourceCategory: Rep[String] = column[String]("source_category")
    /** Database column subscribers SqlType(text) */
    val subscribers: Rep[String] = column[String]("subscribers")
    /** Database column last_key SqlType(text) */
    val lastKey: Rep[String] = column[String]("last_key")
    /** Database column last_update SqlType(timestamp) */
    val lastUpdate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("last_update")
  }
  /** Collection-like TableQuery object for table RssSubscription */
  lazy val RssSubscription = new TableQuery(tag => new RssSubscription(tag))
}
