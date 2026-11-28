package o.lartifa.jam.cool.qq.command

import o.lartifa.jam.cool.qq.command.base.MasterEverywhereCommand
import o.lartifa.jam.onebot.domain.ChatMessage
import o.lartifa.jam.pool.{JamContext, ThreadPools}

import scala.util.control.NonFatal

/**
 * jam_self 自有指令壳：解析前缀、异步分发 Master 指令，不再造 legacy EventMessage。
 *
 * Author: sinar
 * 2026/11/28
 */
object MasterCommandShell {

  val DefaultPrefixes: Seq[String] = Seq("！", "!")

  @volatile private var prefixes: Seq[String] = DefaultPrefixes
  @volatile private var commandsByName: Map[String, MasterEverywhereCommand] = Map.empty

  /**
   * 注册 / 重载指令表。
   */
  def register(
    commands: Seq[MasterEverywhereCommand],
    commandPrefixes: Seq[String] = DefaultPrefixes,
  ): Unit = {
    prefixes = commandPrefixes
    commandsByName = commands.flatMap { cmd =>
      cmd.names.map(_.toLowerCase -> cmd)
    }.toMap
  }

  def clear(): Unit = {
    commandsByName = Map.empty
  }

  /**
   * 尝试解析并异步执行指令。
   *
   * @return 恒为 true（对齐 Picq `commandsAlsoCallEvents=true`，命中后仍进消息监听）
   */
  def tryHandle(chat: ChatMessage): Boolean = {
    parse(chat.message, prefixes) match {
      case None => true
      case Some(ParsedCommand(name, args)) =>
        commandsByName.get(name) match {
          case None => true
          case Some(cmd) =>
            ThreadPools.DEFAULT.execute(() => {
              try {
                val out = cmd.run(chat, name, args)
                if (out != null && out.nonEmpty) {
                  chat.respond(out)
                }
              } catch {
                case NonFatal(err) =>
                  Option(JamContext.loggerFactory.get()).foreach { factory =>
                    factory.getLogger(MasterCommandShell.getClass).error("指令执行失败", err)
                  }
              }
            })
            true
        }
    }
  }

  final case class ParsedCommand(name: String, args: List[String])

  /**
   * 解析指令。三种会话均需前缀（相对 legacy 私聊可无前缀是刻意收紧）。
   */
  def parse(raw: String, commandPrefixes: Seq[String] = prefixes): Option[ParsedCommand] = {
    var text = raw
    while (text.nonEmpty && Character.isWhitespace(text.charAt(0))) {
      text = text.substring(1)
    }
    val matchedPrefix = commandPrefixes.find(p => text.startsWith(p)).getOrElse {
      return None
    }
    text = text.substring(matchedPrefix.length)
    while (text.nonEmpty && Character.isWhitespace(text.charAt(0))) {
      text = text.substring(1)
    }
    if (text.isEmpty) return None

    // 对齐 CommandArgsParser：末尾补 " ;" 再按空格拆，丢掉末段
    val tokens = (text + " ;").split(" ").toList.dropRight(1)
    if (tokens.isEmpty || tokens.head.isEmpty) return None
    Some(ParsedCommand(tokens.head.toLowerCase, tokens.tail))
  }
}
