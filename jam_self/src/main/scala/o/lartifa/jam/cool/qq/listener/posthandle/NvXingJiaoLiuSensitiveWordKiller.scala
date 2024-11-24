package o.lartifa.jam.cool.qq.listener.posthandle

import cc.moecraft.icq.event.events.message.{EventGroupMessage, EventMessage}
import cc.moecraft.icq.sender.message.components.ComponentAt
import o.lartifa.jam.common.util.TriBoolValue
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.plugins.nv_xing_jiao_liu.SensitiveWordKiller
import o.lartifa.jam.pool.JamContext

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * 女性♂交流同人功能
 * 在群里抓取聊天内容并分析拼音，如果存在敏感词则每个词禁言一分钟
 *
 * Author: sinar
 * 2024/11/24 21:26
 */
object NvXingJiaoLiuSensitiveWordKiller extends PostHandleTask("女性♂交流", TriBoolValue.Both) {
  /**
   * 执行
   *
   * @param event      消息对象
   * @param contextOpt 执行上下文
   * @param exec       异步上下文
   * @return 异步返回执行结果
   */
  override def execute(event: EventMessage, contextOpt: Option[CommandExecuteContext])(implicit exec: ExecutionContext): Future[Unit] = async {
    event match {
      case message: EventGroupMessage =>
        val result = await(JamContext.variablePool.get(s"Group_${message.getGroupId}_NXJL_Enabled"))
        if (result.exists(_.toBoolean)) {
          val words = SensitiveWordKiller.matching(event.message)
          if (words.nonEmpty) {
            event.respond(s"${new ComponentAt(event.getSenderId)} 发现：${words.mkString("，")}${if (words.sizeIs > 1) s"\n${words.size}连击！" else ""}")
            event.getHttpApi.setGroupBan(message.getGroupId, message.getSenderId, 60 * words.size)
          }
        }
      case _ =>
    }
  }
}
