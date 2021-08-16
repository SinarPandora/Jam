package o.lartifa.jam.plugins.trpg.command

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.ShellLikeCommand
import o.lartifa.jam.plugins.trpg.command.CreateGame.logger
import o.lartifa.jam.plugins.trpg.data.TRPGData
import o.lartifa.jam.plugins.trpg.data.TRPGGameData.TRPGGameInitData
import o.lartifa.jam.plugins.trpg.rule.RuleRepo
import o.lartifa.jam.pool.JamContext

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * 创建游戏指令
 *
 * Author: sinar
 * 2021/8/16 01:18
 */
case class CreateGame(prefixes: List[String]) extends ShellLikeCommand(prefixes) {
  /**
   * 执行
   *
   * @param prefix  指令前缀
   * @param args    指令参数
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute(prefix: String, args: List[String])(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    if (args.isEmpty) help()
    else {
      val name = args.head
      val ruleName = if (args.sizeIs > 1) args(1) else "默认"
      RuleRepo.rules.get(ruleName) match {
        case Some(_) =>
          val gameId = await(TRPGData.createGame(TRPGGameInitData(name, ruleName, senderId.toString)))
          reply(s"游戏：${name}已创建，Id为：$gameId")
        case None =>
          reply(s"规则：${ruleName}不存在")
      }
    }
  }.recover(err => {
    logger.error(err)
    reply("游戏创建失败，请稍后重试")
  }).map(_ => ())

  /**
   * 输出指令帮助信息
   * 若帮助信息过长，请手动分隔发送
   *
   * @return 帮助信息
   */
  override def help()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = Future {
    reply(
      s"""创建游戏
         |根据规则创建一个 TRPG 游戏，创建者会自动担任游戏的 KP
         |前缀：${prefixes.mkString(" 或 ")}
         |模式：前缀 游戏名 [规则名]
         |举例：${prefixes.head} 巴别塔之茧 默认
         |1. 如果游戏名包含空格，请使用英文双引号包括
         |2. 规则名可选，默认使用：默认规则""".stripMargin)
  }
}

object CreateGame {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(this.getClass)
}
