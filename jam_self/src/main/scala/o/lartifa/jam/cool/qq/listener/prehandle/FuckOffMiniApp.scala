package o.lartifa.jam.cool.qq.listener.prehandle

import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.pool.JamContext

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

/**
 * Author: sinar
 * 2020/8/30 02:49
 */
object FuckOffMiniApp extends PreHandleTask("替换小程序跳转") {
  /*
  Example Message: [CQ:rich,data={"app":"com.tencent.miniapp_01","config":{"autoSize":0,"ctime":1598753523,"forward":1,"height":0,"token":"0aa74dddd3b6798f29e0b6e32db4f5cf","type":"normal","width":0},"desc":"哔哩哔哩","extra":{"app_type":1,"appid":100951776},"meta":{"detail_1":{"appid":"1109937557","desc":"【沙雕方舟】② 小 羊 的 泳 装","host":{"nick":"椎名心阳","uin":1211402231},"icon":"http://miniapp.gtimg.cn/public/appicon/432b76be3a548fc128acaa6c1ec90131_200.jpg","preview":"pubminishare-30161.picsz.qpic.cn/9a4e89ef-9f6c-4459-b856-9d792e61c3ff","qqdocurl":"https://b23.tv/krQBjE?share_medium=android&share_source=qq&bbid=XYDC011D2FFEEF7BEC5604BA2900128BAEF7A&ts=1598753511388","scene":1036,"shareTemplateData":{},"shareTemplateId":"8C8E89B49BE609866298ADDFF2DBABA4","title":"哔哩哔哩","url":"m.q.qq.com/a/s/096bb10b3c212f5f3ebc4a4f2aae2df6"}},"needShareCallBack":false,"prompt":"[QQ小程序]哔哩哔哩","ver":"1.0.0.19","view":"view_8C8E89B49BE609866298ADDFF2DBABA4"}]当前版本不支持该消息类型，请使用最新版本手机QQ查看
   */
  private val RICH_MESSAGE_REGEX_DOC: Regex = """\[CQ:rich,data=\{"app".+?"qqdocurl":"(.+?)".+]""".r("url")
  private val RICH_MESSAGE_REGEX_JUMP: Regex = """\[CQ:rich,data=\{"app".+?"jumpUrl":"(.+?)".+]""".r("url")
  private val RICH_MESSAGE_REGEX_XML: Regex = """\[CQ:rich,data=<\?xml.+?url="(.+?)".+]""".r("url")

  /**
   * 执行前置任务
   *
   * @param event 消息对象（注意此时还没开始进行 SSDL 解析）
   * @param exec  异步上下文
   * @return 如果返回 false，将打断后续的 SSDL 执行
   */
  override def execute(event: EventMessage)(implicit exec: ExecutionContext): Future[Boolean] = async {
    if (event.message.contains("聊天记录")) false
    else {
      val regex = if (event.message.contains("qqdocurl")) RICH_MESSAGE_REGEX_DOC
      else if (event.message.contains("<?xml")) RICH_MESSAGE_REGEX_XML
      else RICH_MESSAGE_REGEX_JUMP
      val found = regex.findFirstMatchIn(event.message).map(result => {
        val url = result.group("url").replaceAll("""\\/""", "\\")
        event.respond(s"小程序地址为：$url")
      }).isDefined
      if (found) await(JamContext.messagePool.recordAPlaceholder(event, "小程序地址解析并发送"))
      true
    }
  }
}
