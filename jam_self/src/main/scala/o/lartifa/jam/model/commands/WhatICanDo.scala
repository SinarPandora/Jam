package o.lartifa.jam.model.commands

import o.lartifa.jam.model.patterns.ContentMatcher
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.pool.JamContext

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
 * "展示果酱可以做什么" 指令
 *
 * Author: sinar
 * 2020/11/15 22:06
 */
object WhatICanDo extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    val ChatInfo(chatType, chatId) = ChatInfo(context.eventMessage)
    val matchers: Seq[ContentMatcher] = (JamContext.customMatchers.get()
      .getOrElse(chatType, Map())
      .getOrElse(chatId, List()) ++ JamContext.globalMatchers.get())
      // 由于正则不是人人能看懂的，所以这里过滤掉正则触发的语句
      .filterNot(_.`type` == ContentMatcher.REGEX)
    val idx = Random.nextInt(matchers.size)
    val matcher = matchers(idx)
    val intro = await(matcher.intro)
    respond(
      s"""你可以尝试$intro
         |------------------------
         |注意，某些功能：
         |1. 可能是概率触发的
         |2. 可能是为指定群定制的""".stripMargin)
  }
}
