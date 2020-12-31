package o.lartifa.jam.engine

import better.files.File
import o.lartifa.jam.common.config.SystemConfig
import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.engine.ssdl.parser.{CommandParser, Parser, PatternParser, Patterns}
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.model.patterns.ParseResult

import java.nio.charset.Charset
import scala.annotation.tailrec
import scala.collection.parallel.CollectionConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * SSDL 解析引擎
 *
 * Author: sinar
 * 2020/1/4 22:41
 */
object SSDLParseEngine extends Parser {

  sealed case class ParseSuccessResult(lineId: Long, filepath: String, result: ParseResult, chatInfo: ChatInfo, name: Option[String] = None)

  sealed case class ParseFailResult(lineId: Long, filepath: String, message: String)

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
  def load()(implicit exec: ExecutionContext): Future[Map[Boolean, Seq[Either[ParseFailResult, ParseSuccessResult]]]] = Future {
    CommandParser.prepareParsers()
    loadFiles().flatMap {
      case (ssdlFiles, chatInfo) => parseFiles(ssdlFiles, chatInfo)
    }.groupBy(_.isRight)
  }

  /**
   * 加载 SSDL 文件
   *
   * @return 文件列表
   */
  private def loadFiles(): List[(List[File], ChatInfo)] = {
    import SystemConfig._
    File(ssdlPath).list.filterNot(f => f.isRegularFile || f.pathAsString.contains("modes")).map { dir =>
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
      dir.listRecursively.filter(file => ssdlFileExtension.contains(file.extension.getOrElse(""))).toList -> chatInfo
    }.toList
  }

  /**
   * 并行解析文本内容
   *
   * @param ssdlFiles 文件列表
   * @param chatInfo  会话信息（针对非全局步骤）
   * @return 解析结果
   */
  private def parseFiles(ssdlFiles: List[File], chatInfo: ChatInfo): Seq[Either[ParseFailResult, ParseSuccessResult]] = {
    ssdlFiles.par.flatMap(parseFileContent(_, chatInfo)).seq
  }

  /**
   * 解析文件内容
   *
   * @param file     文件对象
   * @param chatInfo 会话信息（针对非全局步骤）
   * @return 解析结果
   */
  private def parseFileContent(file: File, chatInfo: ChatInfo): Iterable[Either[ParseFailResult, ParseSuccessResult]] = {
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
          failResult.asInstanceOf[Either[ParseFailResult, ParseSuccessResult]]
        case Right((EffectiveLine(name, step, id), idx)) =>
          parseSSDL(step, id, file.pathAsString, idx + 1, chatInfo, name)
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
                                 effectiveLines: List[Either[ParseFailResult, EffectiveLineIdPair]] = Nil):
  List[Either[ParseFailResult, EffectiveLineIdPair]] = {
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
                Left(ParseFailResult(currentLineId, filepath, "书写内容没有以标准格式开头")))
          }
        }
      case Nil => lastEffectiveLine
        .map(line => effectiveLines :+ Right(line))
        .getOrElse(effectiveLines)
    }
  }

  /**
   * 解析 SSDL
   *
   * @param string   待解析字符串
   * @param id       行前 Id
   * @param filepath 文件路径
   * @param lineId   行号
   * @param chatInfo 会话信息（针对非全局步骤）
   * @return 解析结果
   */
  private def parseSSDL(string: String, id: String, filepath: String, lineId: Long, chatInfo: ChatInfo, name: Option[String]): Either[ParseFailResult, ParseSuccessResult] = {
    Try(PatternParser.parseBasePattern(string, id)) match {
      case Failure(exception) => Left(ParseFailResult(lineId, filepath, exception.getMessage))
      case Success(result) => Right(ParseSuccessResult(lineId, filepath, result, chatInfo, name))
    }
  }
}
