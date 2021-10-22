package o.lartifa.jam.plugins.trpg.command.manage

import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.ShellLikeCommand
import o.lartifa.jam.plugins.trpg.command.manage.GameMange.*

import scala.async.Async.async
import scala.concurrent.{ExecutionContext, Future}

/**
 * 游戏管理
 * 与游戏本身相关的功能
 *
 * Author: sinar
 * 2021/9/9 23:22
 */
case class GameMange(prefixes: Set[String]) extends ShellLikeCommand(prefixes) {
  /**
   * 执行
   *
   * @param args    指令参数
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute(args: List[String])(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = {
    if (args.isEmpty) help()
    else args.head match {
      case "create" | "创建" => createGame.execute(args.tail)
      case "load" | "载入" => loadGame.execute(args.tail)
      case "info" | "show" | "详情" => showGame.execute(args.tail)
      case "update" | "更新" => updateGame.execute(args.tail)
      case "release" | "exit" | "退出" => saveAndExit.execute(args.tail)
      case "list" | "获取全部" => listGame.execute(args.tail)
      case "broadcast" | "广播" => broadcast.execute(args.tail)
      case _ => help()
    }
  }

  /**
   * 输出指令帮助信息
   * 若帮助信息过长，请手动分隔发送
   *
   * @return 帮助信息
   */
  override def help()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    reply(
      s"""游戏管理指令
         |对游戏进行管理
         |前缀：${prefixes.mkString(" 或 ")}
         |模式：前缀 操作 [参数...]
         |可选的操作如下（选择中英文其一即可）：
         |1. 创建/create
         |2. 载入/load
         |3. 详情/info/show
         |4. 更新/update
         |5. 退出/release/exit
         |6. 获取全部/list
         |7. 广播/broadcast
         |--------------------
         |若第一个参数为 help/帮助，即可查看对应功能的帮助信息""".stripMargin)
  }
}

object GameMange {
  private val createGame: CreateGame = CreateGame(Set.empty)
  private val loadGame: LoadGame = LoadGame(Set.empty)
  private val showGame: ShowGame = ShowGame(Set.empty)
  private val updateGame: UpdateGame = UpdateGame(Set.empty)
  private val saveAndExit: SaveAndExit = SaveAndExit(Set.empty)
  private val listGame: ListGame = ListGame(Set.empty)
  private val broadcast: Broadcast = Broadcast(Set.empty)
}
