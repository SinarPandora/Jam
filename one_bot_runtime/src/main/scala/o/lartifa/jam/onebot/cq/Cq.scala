package o.lartifa.jam.onebot.cq

/**
 * CQ 码编解码与组件（对齐 legacy Component* / CQUtils）。
 *
 * Author: sinar
 * 2026/11/27
 */
object Cq {

  /** 解码 OneBot 对 CQ 特殊字符的转义（对齐 CQUtils.decodeMessage）。 */
  def decodeMessage(raw: String): String =
    if (raw == null) ""
    else
      raw
        .replace("&amp;", "&")
        .replace("&#91;", "[")
        .replace("&#93;", "]")
        .replace("&#44;", ",")

  def removeCqCode(original: String): String =
    if (original == null) ""
    else original.replaceAll("""\[CQ:.*?\]""", "")

  sealed trait Component {
    def toCq: String
    override def toString: String = toCq
  }

  final case class At(id: Long) extends Component {
    def toCq: String = s"[CQ:at,qq=$id]"
  }

  final case class Image(fileOrUrl: String) extends Component {
    def toCq: String = s"[CQ:image,file=$fileOrUrl]"
  }

  final case class ImageBase64(base64: String) extends Component {
    def toCq: String = s"[CQ:image,file=base64://$base64]"
  }

  final case class ImageNoCache(fileOrUrl: String) extends Component {
    def toCq: String = s"[CQ:image,cache=0,file=$fileOrUrl]"
  }

  final case class Face(faceId: Int) extends Component {
    def toCq: String = s"[CQ:face,id=$faceId]"
  }

  final case class Record(fileOrUrl: String) extends Component {
    def toCq: String = s"[CQ:record,file=$fileOrUrl]"
  }

  final case class Video(file: String) extends Component {
    def toCq: String = s"[CQ:video,file=$file]"
  }

  final case class Poke(qId: Long) extends Component {
    def toCq: String = s"[CQ:poke,qq=$qId]"
  }

  final case class Tts(text: String) extends Component {
    def toCq: String = s"[CQ:tts,text=$text]"
  }

  final case class Share(url: String, title: String, content: String, image: String) extends Component {
    def toCq: String = s"[CQ:share,url=$url,title=$title,content=$content,image=$image]"
  }

  final case class Location(lat: Double, lon: Double, title: String, content: String) extends Component {
    def toCq: String = s"[CQ:location,lat=$lat,lon=$lon,title=$title,content=$content]"
  }

  final case class Music(musicId: Int, source: MusicSource) extends Component {
    def toCq: String = s"[CQ:music,type=${source.wire},id=$musicId]"
  }

  enum MusicSource(val wire: String) {
    case Qq extends MusicSource("qq")
    case Netease extends MusicSource("163")
    case Xm extends MusicSource("xm")
  }

  final case class Contact(qId: Long, contactType: ContactType) extends Component {
    def toCq: String = s"[CQ:contact,type=${contactType.wire},id=$qId]"
  }

  enum ContactType(val wire: String) {
    case Qq extends ContactType("qq")
    case Group extends ContactType("group")
  }

  case object Dice extends Component {
    def toCq: String = "[CQ:dice,type=1]"
  }

  case object Rps extends Component {
    def toCq: String = "[CQ:rps,type=1]"
  }

  case object Shake extends Component {
    def toCq: String = "[CQ:shake]"
  }

  final class MessageBuilder {
    private val parts = scala.collection.mutable.ArrayBuffer.empty[Any]

    def add(part: Any): MessageBuilder = {
      parts += part
      this
    }

    def newLine(): MessageBuilder = add("\n")

    override def toString: String =
      parts.map {
        case c: Component => c.toCq
        case other        => other.toString
      }.mkString
  }
}
