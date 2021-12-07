package o.lartifa.jam.plugins.lambda.runner

import better.files.*
import cc.moecraft.logger.HyLogger
import cn.hutool.core.io.resource.ResourceUtil
import groovy.lang.Binding
import groovy.util.GroovyScriptEngine
import o.lartifa.jam.common.config.botConfigFile
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.plugins.lambda.context.LambdaContext
import o.lartifa.jam.plugins.lambda.wrapper.DBVarPoolWrapper
import o.lartifa.jam.pool.{JamContext, ThreadPools}

import java.lang.reflect.Method
import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

/**
 * 脚本运行器
 *
 * Author: sinar
 * 2021/11/11 23:10
 */
object ScriptRunner {
  private lazy val scriptRootPath: File = botConfigFile.getString("lambda.script_root_path").toFile.createDirectoryIfNotExists()
  private lazy val groovyEngine: GroovyScriptEngine = new GroovyScriptEngine(Array(scriptRootPath.url), ClassLoader.getSystemClassLoader)
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(ScriptRunner.getClass)
  private var dslWrapper: Method = _

  /**
   * 初始化
   */
  def init(): Unit = {
    dslWrapper = groovyEngine.getGroovyClassLoader
      .parseClass(ResourceUtil.readUtf8Str("wrapper/LambdaDSLWrapper.groovy"))
      .getMethod("setUp", classOf[Binding], classOf[CommandExecuteContext])
    logger.log(s"""Jam Lambda 脚本引擎初始化完成""")
    logger.log("脚本会随着更改自动更新，无需重启 Bot")
  }

  /**
   * 运行脚本
   *
   * @param scriptPath 脚本路径
   * @param ctx        指令执行上下文
   * @param args       用户输入参数
   */
  @throws[ExecutionException]
  def eval(scriptPath: String, ctx: CommandExecuteContext, args: Map[String, Any]): Future[Object] = Future {
    if ((scriptRootPath / scriptPath.stripPrefix("/")).notExists()) throw ExecutionException(s"Lambda 脚本不存在：$scriptPath")
    val binding = new Binding()
    binding.setVariable("$ctx", new LambdaContext(ctx))
    binding.setVariable("$ec", ThreadPools.LAMBDA)
    binding.setVariable("$vars", new DBVarPoolWrapper(ctx.vars, ctx.eventMessage)(ThreadPools.LAMBDA))
    binding.setVariable("$temp", ctx.tempVars._commandScopeParameters.asJava)
    binding.setVariable("$msg", ctx.eventMessage)
    binding.setVariable("log", logger)
    binding.setVariable("$bot", JamContext.bot.get())
    binding.setVariable("$api", JamContext.httpApi.get()())
    args.foreach {
      case (key, value) => binding.setVariable(key, value)
    }
    dslWrapper.invoke(null, binding, ctx)
    try {
      groovyEngine.run(scriptPath, binding)
    } catch {
      case e: Throwable =>
        logger.error("执行脚本过程中出错", e)
        throw ExecutionException(s"执行脚本过程中出错：${e.getMessage}")
    }
  }(ThreadPools.LAMBDA)
}
