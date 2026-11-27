package o.lartifa.jam.onebot

import o.lartifa.jam.ob.PicqBotX
import o.lartifa.jam.ob.accounts.BotAccount
import o.lartifa.jam.ob.sender.IcqHttpApi
import o.lartifa.jam.ob.sender.HttpApiNode
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import o.lartifa.jam.onebot.OneBotHttpClient

import java.util
import scala.jdk.CollectionConverters.*

/**
 * IcqHttpApi 适配器：出站走 ZIO OneBotHttpClient；表面保留在 one_bot_legacy（o.lartifa.jam.ob）。
 *
 * Author: sinar
 * 2026/11/26
 */
class OneBotIcqHttpApiAdapter(
  bot: PicqBotX,
  account: BotAccount,
  host: String,
  port: Int,
  client: OneBotHttpClient,
) extends IcqHttpApi(bot, account, host, port) {

  override def send(api: HttpApiNode, params: util.Map[String, Object]): JsonElement = {
    val path = resolvePath(api)
    val scalaParams: Map[String, Any] =
      if (params == null || params.isEmpty) Map.empty
      else params.asScala.map { case (k, v) => k -> (v: Any) }.toMap
    val body = client.postParams(path, scalaParams)
    JsonParser.parseString(body)
  }

  private def resolvePath(api: HttpApiNode): String = {
    var url = api.getSubUrl
    if (api.isCanAsync) {
      if (bot.getConfig.isApiRateLimited) url = url + "_rate_limited"
      else if (bot.getConfig.isApiAsync) url = url + "_async"
    }
    url.stripPrefix("/")
  }
}

object OneBotIcqHttpApiAdapter {

  /**
   * 把账号管理器里已有账号的 HttpApi 换成适配器。
   * @param reuse 若已有共享客户端（与 OutboundGateway 同源）则复用。
   */
  def installOnAccounts(
    bot: PicqBotX,
    host: String,
    port: Int,
    accessToken: String = "",
    reuse: Option[OneBotHttpClient] = None,
  ): OneBotHttpClient = {
    val client = reuse.getOrElse(new OneBotHttpClient(host, port, accessToken))
    bot.getAccountManager.getAccounts.forEach { account =>
      val adapter = new OneBotIcqHttpApiAdapter(bot, account, host, port, client)
      account.setHttpApi(adapter)
    }
    client
  }
}
