package o.lartifa.jam.engine

import better.files.File
import o.lartifa.jam.common.config.{JamConfig, SystemConfig}
import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.engine.proto.Parser
import o.lartifa.jam.engine.ssdl.parser._
import o.lartifa.jam.engine.stdl.parser.{STDLParseResult, STDLParser}
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.model.patterns.SSDLParseResult

import java.nio.charset.Charset
import scala.annotation.tailrec
import scala.async.Async.{async, await}
import scala.collection.parallel.CollectionConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * SSDL 解析引擎
 *
 * Author: sinar
 * 2020/1/4 22:41
 */
object SXDLParseEngine extends Parser {

  sealed trait SXDLParseSuccessResult

  case class SSDLParseSuccessResult(lineId: Long, filepath: String, result: SSDLParseResult, chatInfo: ChatInfo, name: Option[String] = None) extends SXDLParseSuccessResult

  case class STDLParseSuccessResult(lineId: Long, filepath: String, result: STDLParseResult.Succ, chatInfo: ChatInfo, name: Option[String] = None) extends SXDLParseSuccessResult

  case class SXDLParseFailResult(lineId: Long, filepath: String, message: String)

  implicit val charset: Charset = Charset.forName("UTF-8")

  type RawLine = (Option[String], String)
  type RawLinePair = (RawLine, Int)

  case class EffectiveLine(name: Option[String], line: String, id: String)

  type EffectiveLineIdPair = (EffectiveLine, Int)


  /**
   * 加载并解析 SSDL
   *
   * @param exec 异步执行上下文
   * @return 解析结果，键为 false 对应的内容为解析失败的信息
   */
  @throws[ParseFailException]
  def load()(implicit exec: ExecutionContext): Future[Map[Boolean, Seq[Either[SXDLParseFailResult, SXDLParseSuccessResult]]]] = async {
    CommandParser.prepareParsers()
    val scriptPath: File = File(SystemConfig.sxdlPath).createDirectoryIfNotExists()
    if (JamConfig.RemoteEditing.enable) {
      await(RemoteSXDLClient.fetchRemoteScripts(scriptPath))
    }
    loadFiles(scriptPath).flatMap {
      case (ssdlFiles, chatInfo) => parseFiles(ssdlFiles, chatInfo)
    }.groupBy(_.isRight)
  }

  /**
   * 加载 SSDL 文件
   *
   * @param scriptPath SXDL 脚本路径
   * @return 文件列表
   */
  private def loadFiles(scriptPath: File): List[(List[File], ChatInfo)] = {
    import SystemConfig._
    scriptPath.list.filterNot(f => f.isRegularFile || f.pathAsString.contains("modes"))
      .filterNot(_.name.startsWith("."))
      .map { dir =>
      // 忽略备注 + 获取会话格式
      val dirName = dir.name.split("[）)]").last
      val chatInfo = dirName match {
        case "global" => ChatInfo.None
        case "global_private" => ChatInfo.Private
        case "global_group" => ChatInfo.Group
        case _ =>
          val split = dirName.split("_")
          need(split.length == 2, s"文件夹名：${dir.pathAsString}格式不正确（起名格式：global，global_private, global_group，private_xxx, group_xxx）")
          val Array(tp, id) = split.take(2)
          ChatInfo(tp, id.toLong)
      }
      dir.listRecursively.filter(file => sxdlFileExtension.contains(file.extension.getOrElse(""))).toList -> chatInfo
    }.toList
  }

  /**
   * 并行解析文本内容
   *
   * @param ssdlFiles 文件列表
   * @param chatInfo  会话信息（针对非全局步骤）
   * @return 解析结果
   */
  private def parseFiles(ssdlFiles: List[File], chatInfo: ChatInfo): Seq[Either[SXDLParseFailResult, SXDLParseSuccessResult]] = {
    ssdlFiles.par.flatMap(parseFileContent(_, chatInfo)).seq
  }

  /**
   * 解析文件内容
   *
   * @param file     文件对象
   * @param chatInfo 会话信息（针对非全局步骤）
   * @return 解析结果
   */
  private def parseFileContent(file: File, chatInfo: ChatInfo): Iterable[Either[SXDLParseFailResult, SXDLParseSuccessResult]] = {
    val lineWithIdxPairs: List[RawLinePair] = file.lines
      .map(_.trim)
      .map(line => {
        if (line.startsWith("(") || line.startsWith("（")) {
          val (name, step) = line.splitAt(line.indexWhere(c => c == ')' || c == '）') + 1)
          Some(name.substring(1, name.length - 1)) -> step
        } else None -> line
      })
      .zipWithIndex
      .filterNot { case ((_, line), _) => line.startsWith("#") || line.isEmpty }
      .toList

    // 找到所有有效行并解析
    findEffectiveLines(file.pathAsString, lineWithIdxPairs)
      .map {
        case failResult@Left(_) =>
          failResult.asInstanceOf[Either[SXDLParseFailResult, SSDLParseSuccessResult]]
        case Right((EffectiveLine(name, step, id), lineId)) =>
          Try(id.toLong) match {
            case Failure(_) =>
              Left(SXDLParseFailResult(lineId, file.pathAsString, "步骤编号过大，过小或不合法"))
            case Success(id) =>
              val context = preprocessStatement(step, id)
              parseSXDL(context.processedStr, context, file.pathAsString, lineId + 1, chatInfo, name)
          }
      }
  }

  /**
   * 寻找所有有效行
   * 规则：
   * 1. 如果行前没有 ID，将其与最近的有 ID 行拼接
   * 2. 如果能与之拼接的行不存在（第一行就没有 ID），则视为解析失败
   * 3. 如果开头是竖线，保留换行，否则将换行删掉
   *
   * @param filepath          文件路径
   * @param pairs             行，ID 对
   * @param lastEffectiveLine 最近的一个有效行
   * @param effectiveLines    找到的全部有效行（递归缓存结果）
   * @return 全部有效行
   */
  @tailrec
  private def findEffectiveLines(filepath: String, pairs: List[RawLinePair], lastEffectiveLine: Option[EffectiveLineIdPair] = None,
                                 effectiveLines: List[Either[SXDLParseFailResult, EffectiveLineIdPair]] = Nil):
  List[Either[SXDLParseFailResult, EffectiveLineIdPair]] = {
    pairs match {
      case ((name, line), currentLineId) :: next =>
        Patterns.basePattern.findFirstMatchIn(line) match {
          // 如果出现下一个带有 ID 的行，就说明上一个行组解析完毕了
          case Some(result) =>
            val content = result.group("content")
            val id = result.group("id")
            val newEffectiveLinePair = (EffectiveLine(name, content, id), currentLineId)
            lastEffectiveLine match {
              case Some(effectiveLine) =>
                findEffectiveLines(filepath, next, Some(newEffectiveLinePair), effectiveLines :+ Right(effectiveLine))
              case None =>
                findEffectiveLines(filepath, next, Some(newEffectiveLinePair), effectiveLines)
            }
          case None => lastEffectiveLine match {
            case Some(pair@(effectiveLine@EffectiveLine(_, lastLine, _), _)) => if (line.startsWith("|")) {
              // 如果开头是竖线，保留换行
              findEffectiveLines(filepath, next, Some(pair.copy(_1 = effectiveLine.copy(line = lastLine + "\n" + line))),
                effectiveLines)
            } else {
              findEffectiveLines(filepath, next, Some(pair.copy(_1 = effectiveLine.copy(line = lastLine + line))),
                effectiveLines)
            }
            case None =>
              findEffectiveLines(filepath, next, None, effectiveLines :+
                Left(SXDLParseFailResult(currentLineId, filepath, "书写内容没有以标准格式开头")))
          }
        }
      case Nil => lastEffectiveLine
        .map(line => effectiveLines :+ Right(line))
        .getOrElse(effectiveLines)
    }
  }

  /**
   * 解析 SXDL：SSDL/STDL
   *
   * @param string   待解析字符串
   * @param context  解析引擎上下文
   * @param filepath 文件路径
   * @param lineId   行号
   * @param chatInfo 会话信息（针对非全局步骤）
   * @return 解析结果
   */
  private def parseSXDL(string: String, context: ParseEngineContext, filepath: String, lineId: Long, chatInfo: ChatInfo, name: Option[String]): Either[SXDLParseFailResult, SXDLParseSuccessResult] = {
    // 判断是 STDL 还是 SSDL
    STDLParser.stdlPattern.findFirstMatchIn(string) match {
      case Some(result) => parseSTDL(result.group("cron"), result.group("action"), context, filepath, lineId, chatInfo, name)
      case None => parseSSDL(string, context, filepath, lineId, chatInfo, name)
    }
  }

  /**
   * 解析 STDL
   *
   * @param rawDTExp   待解析时间表达式
   * @param rawCommand 待解析指令
   * @param context    解析引擎上下文
   * @param filepath   文件路径
   * @param lineId     行号
   * @param chatInfo   会话信息（针对非全局步骤）
   * @return 解析结果
   */
  private def parseSTDL(rawDTExp: String, rawCommand: String, context: ParseEngineContext, filepath: String, lineId: Long, chatInfo: ChatInfo, name: Option[String]): Either[SXDLParseFailResult, STDLParseSuccessResult] = {
    STDLParser.parseSTDL(rawDTExp, rawCommand)(context) match {
      case result: STDLParseResult.Succ => Right(STDLParseSuccessResult(lineId, filepath, result, chatInfo, name))
      case STDLParseResult.Fail(exception) => Left(SXDLParseFailResult(lineId, filepath, exception.getMessage))
    }
  }

  /**
   * 解析 SSDL
   *
   * @param string   待解析字符串
   * @param context  解析引擎上下文
   * @param filepath 文件路径
   * @param lineId   行号
   * @param chatInfo 会话信息（针对非全局步骤）
   * @return 解析结果
   */
  private def parseSSDL(string: String, context: ParseEngineContext, filepath: String, lineId: Long, chatInfo: ChatInfo, name: Option[String]): Either[SXDLParseFailResult, SSDLParseSuccessResult] = {
    Try(SSDLParser.parseSSDL(string)(context)) match {
      case Failure(exception) => Left(SXDLParseFailResult(lineId, filepath, exception.getMessage))
      case Success(result) => Right(SSDLParseSuccessResult(lineId, filepath, result, chatInfo, name))
    }
  }

  /**
   * 预处理语句中的变量和模板
   *
   * @param string 待解析字符串
   * @param stepId 步骤 ID
   * @return 解析引擎上下文
   */
  private def preprocessStatement(string: String, stepId: Long): ParseEngineContext = {
    // 1. 找到全部模板，替换为 %{_1}%
    val templates = VarParser.parseTemplates(string).getOrElse(Nil).zipWithIndex.map { case (it, idx) => it -> s"_$idx" }
    val str1 = templates.foldLeft(string) { case (str, (it, idx)) => str.replace(it.source, s"%{$idx}%") }
    // 2. 找到剩余的全部变量，替换掉 {_1}
    val varKeys = VarParser.parseVars(str1).getOrElse(Nil).zipWithIndex.map { case (it, idx) => it -> s"_$idx" }
    val processedStr = varKeys.foldLeft(str1) { case (str, (it, idx)) => str.replace(it.source, s"{$idx}") }
    // 3. 组装解析上下文
    val templateMap = templates.map(it => it._2 -> it._1.command).toMap
    val varKeysMap = varKeys.map(it => it._2 -> it._1.varKey).toMap
    // 4. 去除掉除模板外的全部空格
    ParseEngineContext(stepId, varKeysMap, templateMap, string, processedStr.replaceAll("\\s", ""))
  }
}
