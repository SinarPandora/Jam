package o.lartifa.jam.plugins

/**
 * Author: sinar
 * 2020/7/12 15:03
 */
package object picbot {
  def API(page: Int): String = s"https://konachan.com/post.json?tags=loli&page=$page"

  object PatternRating {
    val SAFE: String = "健全"
    val QUESTIONABLE: String = "HSO"
    val EXPLICIT: String = "H"
  }

  object PatternMode {
    val ONLY: String = "仅当前"
    val RANGE: String = "范围内"
  }

  val CONFIG_PAGE: String = "picbot_page"
  val CONFIG_ALLOWED_RATING: String = "picbot_allowed_rating"
  val CONFIG_MODE: String = "picbot_mode"

  sealed class Rating(val str: String)
  case object SAFE extends Rating("s")
  case object QUESTIONABLE extends Rating("q")
  case object EXPLICIT extends Rating("e")

  sealed class Mode(val str: String)
  case object ONLY extends Mode("only")
  case object RANGE extends Mode("range")
}
