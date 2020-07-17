package o.lartifa.jam.plugins.picbot

import ammonite.ops._
import cc.moecraft.icq.sender.message.components.ComponentImage
import com.jsoniter.JsonIterator
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * Author: sinar
 * 2020/7/13 19:49
 */
case class NewFetchAndSendPic() extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = Future {
    val url = (requests.get("https://api.lolicon.app/setu/?apikey=825095135f0c4ec4cfaa84&r18=2&size1200=true").text()
      |> JsonIterator.parse
      |> (_.readAny())
      |> (_.get("data"))
      |> (_.asList())
      |> (_.get(0))
      |> (_.toString("url")))
    Try(context.eventMessage.respond(new ComponentImage(url).toString))
  }
}
