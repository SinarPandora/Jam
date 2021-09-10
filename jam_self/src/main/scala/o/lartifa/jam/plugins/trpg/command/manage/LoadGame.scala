package o.lartifa.jam.plugins.trpg.command.manage

import akka.actor.ActorRef
import cc.moecraft.icq.event.events.message.EventMessage
import cn.hutool.core.util.NumberUtil
import o.lartifa.jam.common.protocol.{Data, Done, Fail}
import o.lartifa.jam.common.util.ExtraActor
import o.lartifa.jam.cool.qq.listener.interactive.{Interactive, InteractiveSession}
import o.lartifa.jam.database.temporary.schema.Tables
import o.lartifa.jam.model.behaviors.ActorCreator
import o.lartifa.jam.model.commands.ShellLikeCommand
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.plugins.trpg.TRPGGameManager.TRPGManage.{Register, Release}
import o.lartifa.jam.plugins.trpg.TRPGInstance.Get
import o.lartifa.jam.plugins.trpg.data.{TRPGDataRepo, TRPGGameData}
import o.lartifa.jam.plugins.trpg.{gameManager, trpgGameRegistry}

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * 加载游戏指令
 *
 * Author: sinar
 * 2021/8/16 01:49
 */
case class LoadGame(prefixes: Set[String]) extends ShellLikeCommand(prefixes) with Interactive with ActorCreator {
  /**
   * 执行
   *
   * @param args 指令参数
   * @param ctx  执行上下文
   * @param exec 异步上下文
   * @return 异步返回执行结果
   */
  override def execute(args: List[String])(implicit ctx: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = {
    if (args.isEmpty) help()
    else async {
      val idOrName = if (NumberUtil.isLong(args.head)) Left(args.head.toLong) else Right(args.head)
      trpgGameRegistry.get(ctx.chatInfo) match {
        case Some(gameRef) =>
          whenGameExistInSession(gameRef, idOrName)
        case None =>
          await(loadGameByIdOrName(idOrName))
      }
    }
  }

  /**
   * 当前会话中存在游戏时
   *
   * @param existingGame 已存在的游戏实例引用
   * @param idOrName     Id 或名字
   * @param ctx          指令上下文
   * @param exec         异步上下文
   */
  def whenGameExistInSession(existingGame: ActorRef, idOrName: Either[Long, String])(implicit ctx: CommandExecuteContext, exec: ExecutionContext): Unit = {
    actorOf(new ExtraActor() {
      override def onStart(): Unit = existingGame ! Get(self)

      override def handle: Receive = {
        case Data(game: TRPGGameData) =>
          game.chatInfo.toString
          reply(
            s"""本聊天有未退出的游戏：${game.name}，是否退出？
               |输入 "是" 或 "Y" 退出当前游戏并加载指定游戏
               |输入其他内容退出当前指令""".stripMargin)
          interact { (session: InteractiveSession, event: EventMessage) =>
            event.message.trim.toUpperCase match {
              case "是" | "Y" =>
                actorOf(new ExtraActor() {
                  override def onStart(): Unit = gameManager ! Release(ctx.chatInfo, self)

                  override def handle: Receive = {
                    case Done => loadGameByIdOrName(idOrName)
                  }
                })
                session.release()
              case _ => session.release()
            }
          }
      }
    })
  }

  /**
   * 通过 id 或名字加载游戏
   *
   * @param idOrName Id 或名字
   * @param ctx      指令上下文
   * @param exec     异步上下文
   * @return 加载结果
   */
  def loadGameByIdOrName(idOrName: Either[Long, String])(implicit ctx: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    val result = idOrName match {
      case Left(id) => await(TRPGDataRepo.loadGameDataById(id, ctx.chatInfo, ctx.eventMessage.getSenderId))
      case Right(name) =>
        await(TRPGDataRepo.findGameDataByName(name)).toList match {
          case Nil => Left("没找到指定游戏")
          case head :: Nil => await(TRPGDataRepo.loadGameDataById(head.id, ctx.chatInfo, ctx.eventMessage.getSenderId))
          case all => Left(whichGameYouWantToLoad(all))
        }
    }
    result match {
      case Left(errMsg) => reply(errMsg)
      case Right(data) => loadGameToSession(data)
    }
  }

  /**
   * 将游戏加载到会话
   *
   * @param data 游戏数据
   * @param ctx  指令上下文
   */
  private def loadGameToSession(data: TRPGGameData)(implicit ctx: CommandExecuteContext, exec: ExecutionContext): Unit = {
    actorOf(new ExtraActor() {
      override def onStart(): Unit = gameManager ! Register(data, self)

      override def handle: Receive = {
        case _: Data[ActorRef] =>
          reply(
            """加载成功
              |===Link Start！===""".stripMargin)
        case Fail(msg) => reply(msg)
      }
    })
  }

  /**
   * 询问玩家加载哪个游戏
   *
   * @param allGames 全部游戏记录
   * @param ctx      指令上下文
   * @param exec     异步上下文
   * @return 提示语
   */
  private def whichGameYouWantToLoad(allGames: List[Tables.TrpgGameRow])(implicit ctx: CommandExecuteContext, exec: ExecutionContext): String = {
    val chatSer = ctx.chatInfo.serialize
    val games = allGames.find(_.lastChat.exists(_ == chatSer)) match {
      case Some(lastGameInChat) =>
        // 如果能找到之前在本会话玩过的游戏，将其置顶
        lastGameInChat +: allGames.filterNot(_.id == lastGameInChat.id)
      case None => allGames
    }
    val gameIds = games.map(_.id).toSet
    interact { (session: InteractiveSession, event: EventMessage) =>
      event.message.trim match {
        case idStr if NumberUtil.isLong(idStr) =>
          val id = idStr.toLong
          if (gameIds.contains(id)) {
            TRPGDataRepo.loadGameDataById(id, ctx.chatInfo, ctx.eventMessage.getSenderId).foreach {
              case Left(errMsg) => reply(errMsg)
              case Right(data) => loadGameToSession(data)
            }
          } else {
            reply("游戏不存在！")
          }
          session.release()
        case _ => session.release()
      }
    }
    "找到多个同名游戏，请输入选择需要加载的游戏 Id：\n" + games.map(game =>
      s"${game.id}：${game.name}"
        + game.lastChat.map(ChatInfo(_)).map(chat => s"\n上次游玩：$chat").getOrElse("")
    ).mkString("\n") + "\n输入其他内容退出"
  }

  /**
   * 输出指令帮助信息
   * 若帮助信息过长，请手动分隔发送
   *
   * @return 帮助信息
   */
  override def help()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    reply(
      s"""加载游戏到当前会话
         |模式：游戏名/游戏Id
         |举例：巴别塔之茧
         |     12
         |1. 若出现重名游戏，则必须使用游戏Id进行加载
         |2. 每个会话只能存在最多一场游戏，加入新游戏前需要退出之前的游戏""".stripMargin)
  }
}
