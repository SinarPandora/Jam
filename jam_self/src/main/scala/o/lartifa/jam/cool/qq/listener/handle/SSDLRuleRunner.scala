package o.lartifa.jam.cool.qq.listener.handle

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}
import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import cn.hutool.core.date.StopWatch
import o.lartifa.jam.common.config.BotConfig
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.cool.qq.listener.MsgMatchers
import o.lartifa.jam.cool.qq.listener.base.Break
import o.lartifa.jam.cool.qq.listener.fsm.FSMModeRouter
import o.lartifa.jam.model.patterns.ContentMatcher
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.pool.JamContext

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
 * 全局消息解析器
 *
 * Author: sinar
 * 2020/1/2 22:20
 */
object SSDLRuleRunner {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(SSDLRuleRunner.getClass)

  /**
   * 搜索匹配的步骤并启动
   *
   * @param eventMessage 消息对象
   * @param exec         异步执行上下文
   * @return 执行结果，若成功捕获并执行，则返回当次的指令上下文
   */
  def executeIfFound(eventMessage: EventMessage)(implicit exec: ExecutionContext): Future[Option[CommandExecuteContext]] = if (!JamContext.initLock.get()) {
    // 启动计时器
    val matchCost = new StopWatch()
    matchCost.start()
    // 组合捕获器列表
    val scanList = this.buildMatcherScanList(eventMessage)
    // 组建上下文
    implicit val context: CommandExecuteContext = CommandExecuteContext(eventMessage)
    // 判断当前会话是否存在模式
    FSMModeRouter.forward(eventMessage).flatMap(continue => {
      // 是否继续执行普通流程（捕获并执行 SSDL）
      if (continue) {
        // 查找步骤
        findMatchedStep(eventMessage.getMessage, scanList).map { matcher =>
          val stepId = matcher.stepId
          // 执行任务
          val ssdlTask = JamContext.stepPool.get().goto(stepId).recover {
            case break: Break =>
              logger.log(s"SSDL 执行被打断，退出码为：${break.exitCode}")
            case exception =>
              logger.error(exception)
              MasterUtil.notifyMaster(s"%s，步骤${stepId}执行失败了，原因是：${exception.getMessage}")
          }.flatMap(_ => JamContext.messagePool.recordAPlaceholder(eventMessage, "已捕获并执行一次SSDL")).map(_ => ())
          // 输出捕获信息
          matchCost.stop()
          val cost = matchCost.getTotalTimeSeconds
          if (cost < 1) logger.log(s"${AnsiColor.GREEN}成功捕获！步骤ID：$stepId，耗时：小于1s")
          else if (cost < 4) logger.log(s"${AnsiColor.GREEN}成功捕获！步骤ID：$stepId，耗时：${cost}s")
          else logger.warning(s"${AnsiColor.RED}成功捕获但耗时较长，请考虑对捕获条件进行优化。步骤ID：$stepId，耗时：${cost}s")
          ssdlTask.map(_ => Some(context))
        }.getOrElse {
          matchCost.stop()
          Future.successful(None)
        }
      } else Future.successful(None)
    })
  } else Future.successful(None)

  /**
   * 寻找匹配的步骤
   *
   * @param message  消息对象
   * @param matchers 捕获器列表
   * @param context  指令执行上下文
   * @param exec     异步执行上下文
   * @return 匹配结果
   */
  @tailrec private def findMatchedStep(message: String, matchers: List[ContentMatcher])(implicit context: CommandExecuteContext,
                                                                                        exec: ExecutionContext): Option[ContentMatcher] = {
    matchers match {
      case matcher :: next =>
        if (matcher.isMatched(message)) Some(matcher)
        else findMatchedStep(message, next)
      case Nil => None
    }
  }

  /**
   * 构建关键词扫描列表
   *
   * @param eventMessage 聊天事件
   * @return 扫描列表
   */
  private def buildMatcherScanList(eventMessage: EventMessage): List[ContentMatcher] = {
    val ChatInfo(chatType, chatId) = ChatInfo(eventMessage)
    val chatScopeMatchers = MsgMatchers.custom.get().get(chatType).flatMap(_.get(chatId)).getOrElse(List.empty)
    val chatTypeScopeMatchers = eventMessage match {
      case _: EventGroupOrDiscussMessage => MsgMatchers.globalGroup.get()
      case _: EventPrivateMessage => MsgMatchers.globalPrivate.get()
    }
    val globalScopeMatchers = MsgMatchers.global.get()
    if (BotConfig.matchOutOfOrder) {
      Random.shuffle(chatScopeMatchers) ++ Random.shuffle(chatTypeScopeMatchers) ++ Random.shuffle(globalScopeMatchers)
    } else {
      chatScopeMatchers ++ chatTypeScopeMatchers ++ globalScopeMatchers
    }
  }
}
