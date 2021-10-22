package o.lartifa.jam.plugins.trpg.command.manage

import o.lartifa.jam.common.protocol.Data
import o.lartifa.jam.common.util.ExtraActor
import o.lartifa.jam.cool.qq.listener.interactive.Interactive
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.behaviors.ActorCreator
import o.lartifa.jam.model.commands.ShellLikeCommand
import o.lartifa.jam.plugins.trpg.TRPGInstance.Get
import o.lartifa.jam.plugins.trpg.data.{TRPGDataRepo, TRPGGameData}
import o.lartifa.jam.plugins.trpg.trpgGameRegistry

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * 展示游戏数据
 *
 * Author: sinar
 * 2021/8/23 23:44
 */
case class ShowGame(prefixes: Set[String]) extends ShellLikeCommand(prefixes) with Interactive with ActorCreator {
  /**
   * 执行
   *
   * @param args    指令参数
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute(args: List[String])(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = {
    ???
  }

  /**
   * 展示当前会话中的游戏
   *
   * @param ctx     指令上下文
   * @param exec    异步上下文
   * @return 操作结果
   */
  def showSessionGame()(implicit ctx: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    trpgGameRegistry.get(ctx.chatInfo) match {
      case Some(gameRef) => actorOf(new ExtraActor() {
        override def onStart(): Unit = gameRef ! Get(self)

        override def handle: Receive = {
          case Data(data: TRPGGameData) =>

        }
      })
      case None => reply("当前会话没有运行游戏")
    }
  }

  /**
   * 通过 id 或名字展示游戏
   *
   * @param idOrName Id 或名字
   * @param ctx      指令上下文
   * @param exec     异步上下文
   * @return 操作结果
   */
  def showGameByIdOrName(idOrName: Either[Long, String])(implicit ctx: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    val result = idOrName match {
      case Left(id) => await(TRPGDataRepo.loadGameDataById(id, ctx.chatInfo, ctx.eventMessage.getSenderId))
      case Right(name) =>
        await(TRPGDataRepo.findGameDataByName(name)).toList match {
          case Nil => Left("没找到指定游戏")
          case head :: Nil => await(TRPGDataRepo.loadGameDataById(head.id, ctx.chatInfo, ctx.eventMessage.getSenderId))
          case all => Left(???)
        }
    }
    result match {
      case Left(errMsg) => reply(errMsg)
      case Right(data) => ???
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
      s"""展示游戏信息
         |模式：[游戏名/游戏Id]
         |举例：<什么也不填写>
         |     巴别塔之茧
         |     12
         |1. 当不填写游戏名/Id时展示当前游戏的信息
         |2. 若出现重名游戏，则必须使用游戏Id进行加载""".stripMargin)
  }
}
