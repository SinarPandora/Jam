package o.lartifa.jam.backend

import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.common.config.BotConfig.{password, qID}
import o.lartifa.jam.common.config.CoolQConfig
import o.lartifa.jam.common.util.OSUtil
import o.lartifa.jam.pool.JamContext

import java.io.{BufferedReader, File, FileReader}
import java.nio.file.Paths
import scala.sys.process.*
import scala.util.control.Breaks.{break, breakable}

/**
 * Mirai 后端适配器
 *
 * Author: sinar
 * 2020/11/17 20:05
 */
object MiraiBackend extends Backend {
  /**
   * 启动后端
   *
   * @param args        程序参数
   * @param afterBooted 启动后任务（回调）
   */
  override def startAndConnectToBackEnd(args: Array[String])(afterBooted: () => Unit): Unit = {
    JamContext.loggerFactory.get().system.log(s"${AnsiColor.GREEN}正在尝试启动并连接 Miari 后端...")
    val logFile = Paths.get(CoolQConfig.Backend.Mirai.path, "backend.log").toFile
    cleanUpBackendLogfile(logFile)
    (s"""echo "login $qID $password""""
      #| Process("java -jar ./backend.jar", new File(CoolQConfig.Backend.Mirai.path))
      #> logFile).run()
    setUpAutoShutdownBackend()
    // 阻塞检查
    checkUntilStarted(logFile, args)
    afterBooted()
  }

  /**
   * 清理已存在的后端 log 文件
   *
   * @param file log 文件
   */
  private def cleanUpBackendLogfile(file: File): Unit = {
    if (file.exists()) {
      file.delete()
    }
    file.createNewFile()
  }

  /**
   * 设置程序结束时自动关闭 Mirai 后端
   */
  private def setUpAutoShutdownBackend(): Unit = {
    sys addShutdownHook {
      JamContext.loggerFactory.get().system.log(s"${AnsiColor.GREEN}检测到程序关闭，正在停止 Miari 后端...")
      "jcmd -l".!!.lines()
        .filter(it => it.contains("./backend.jar"))
        .map(it => it.split(" ").head)
        .findAny().stream().forEach { pid =>
        if (OSUtil.isWindows) {
          // Is windows
          s"taskkill /F /PID $pid".!
        } else {
          s"kill -15 $pid".!
        }
      }
      JamContext.loggerFactory.get().system.log(s"${AnsiColor.GREEN}Miari 后端成功关闭")
    }
  }

  /**
   * 检查日志文件，直到确定后端成功启动
   *
   * @param args    程序参数
   * @param logFile 日志文件
   */
  private def checkUntilStarted(logFile: File, args: Array[String]): Unit = {
    val reader = new BufferedReader(new FileReader(logFile))
    val successMessage =
      if (args.contains("--use_mirai3")) s"($qID) Login successful"
      else "[NETWORK] ConfigPushSvc.PushReq: Success"

    breakable {
      while (true) {
        val line = reader.readLine()
        if (line == null) Thread.sleep(500)
        else if (line.contains(successMessage)) break()
        else println(line)
      }
    }
    reader.close()

    JamContext.loggerFactory.get().system.log(s"${AnsiColor.GREEN}Miari 后端已启动")
  }
}
