package o.lartifa.jam.onebot

import cc.moecraft.icq.PicqBotX
import cc.moecraft.icq.accounts.BotAccount
import cc.moecraft.icq.sender.IcqHttpApi
import cc.moecraft.icq.sender.HttpApiNode
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import o.lartifa.jam.onebot.OneBotHttpClient

import java.util
import scala.jdk.CollectionConverters.*

/**
 * 临时 IcqHttpApi 适配器：出站走 ZIO OneBotHttpClient，其余 API 表面保持 Picq 兼容（#21 删除）。
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
   */
  def installOnAccounts(bot: PicqBotX, host: String, port: Int, accessToken: String = ""): OneBotHttpClient = {
    val client = new OneBotHttpClient(host, port, accessToken)
    bot.getAccountManager.getAccounts.forEach { account =>
      val adapter = new OneBotIcqHttpApiAdapter(bot, account, host, port, client)
      account.setHttpApi(adapter)
    }
    client
  }
}
