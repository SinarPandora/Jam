package o.lartifa.jam.plugins.push.command

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.ShellLikeCommand

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * 源推送综合指令
 *
 * Author: sinar
 * 2022/6/10 01:06
 */
object SourcePushAllInOneCommand extends ShellLikeCommand(".订阅", "。订阅") {
  /**
   * 执行
   *
   * @param args    指令参数
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute(args: List[String])(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    args match {
      case Nil => await(ListSubscriptionsInChat.execute())
      case "列表" :: _ => await(ListSubscriptionsInChat.execute())
      case cmd :: sourceType :: sourceIdentity :: _ =>
        await(context.tempVars.updateOrElseSet("sourceType", sourceType))
        await(context.tempVars.updateOrElseSet("sourceIdentity", sourceIdentity))
        cmd match {
          case "添加" => await(CreateSubscription.execute())
          case "删除" => await(DeleteSubscription.execute())
          case "暂停" | "恢复" =>
            await(context.tempVars.updateOrElseSet("isPaused", (cmd == "暂停").toString))
            await(PauseOrResumeSubscription.execute())
          case _ => reply("指令不正确，请发送 .订阅 帮助 查看操作提示")
        }
      case _ => reply("指令格式不正确，请发送 .订阅 帮助 查看操作提示")
    }
    ()
  }

  /**
   * 输出指令帮助信息
   * 若帮助信息过长，请手动分隔发送
   *
   * @return 帮助信息
   */
  override def help()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    reply(
      """[帮助] 源订阅指令
        |添加订阅源（如 B站动态，微博）
        |当订阅源更新时，bot 会将内容转发到当前聊天
        |-----------------------------
        |当前支持的订阅源：
        |B站动态：标识为 UID
        |-----------------------------
        |订阅源查看：
        |.订阅 列表
        |
        |订阅源操作：
        |.订阅 添加 <订阅源> <标识>
        |.订阅 删除 <订阅源> <标识>
        |.订阅 暂停 <订阅源> <标识>
        |.订阅 恢复 <订阅源> <标识>
        |-----------------------------
        |举例：
        |.订阅 添加 B站动态 123456""".stripMargin)
  }
}
