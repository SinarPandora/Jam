package o.lartifa.jam.plugins.lambda

import o.lartifa.jam.model.commands.{Command, RenderStrTemplate}
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.plugins.lambda.runner.ScriptRunner

import java.util
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.*

/**
 * 运行 Lambda 指令
 *
 * Author: sinar
 * 2021/11/11 23:54
 */
case class RunLambda(scriptPath: String, vars: Seq[RenderStrTemplate]) extends Command[Object] {
  private val scopedVars: mutable.Map[ChatInfo, util.HashMap[String, Object]] = mutable.HashMap().withDefaultValue(new util.HashMap[String, Object]())
  private val rootScopedVars: util.HashMap[String, Object] = new util.HashMap[String, Object]()
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Object] = {
    Future.sequence(vars.map(_.execute())).flatMap(args => ScriptRunner.eval(scriptPath, context, Map(
      "args" -> args.asJava,
      "$scope" -> scopedVars(context.chatInfo),
      "$rootScope" -> rootScopedVars
    )))
  }
}
