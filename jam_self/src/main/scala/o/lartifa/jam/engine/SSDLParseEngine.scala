package o.lartifa.jam.engine

import java.nio.charset.Charset

import better.files.File
import o.lartifa.jam.common.config.SystemConfig
import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.engine.parser.{Parser, PatternParser}
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.model.patterns.ParseResult

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

  /**
   * 加载并解析 SSDL
   *
   * @param exec 异步执行上下文
   * @return 解析结果，键为 false 对应的内容为解析失败的信息
   */
  @throws[ParseFailException]
  def load()(implicit exec: ExecutionContext): Future[Map[Boolean, Seq[Either[ParseFailResult, ParseSuccessResult]]]] = Future {
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
      if (dirName == "global") {
        dir.listRecursively.filter(file => ssdlFileExtension.contains(file.extension.getOrElse(""))).toList -> ChatInfo.None
      } else {
        val split = dirName.split("_")
        need(split.length == 2, s"文件夹名：${dir.pathAsString}格式不正确（起名格式：global，private_xxx, group_xxx，discuss_xxx）")
        val Array(tp, id) = split.take(2)
        dir.listRecursively.filter(file => ssdlFileExtension.contains(file.extension.getOrElse(""))).toList -> ChatInfo(tp, id.toLong)
      }
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
    ssdlFiles.flatMap(parseFileContent(_, chatInfo))
  }

  /**
   * 解析文件内容
   *
   * @param file     文件对象
   * @param chatInfo 会话信息（针对非全局步骤）
   * @return 解析结果
   */
  private def parseFileContent(file: File, chatInfo: ChatInfo): Iterable[Either[ParseFailResult, ParseSuccessResult]] = {
    file.lines
      .map(_.trim)
      .map(line => {
        if (line.startsWith("(") || line.startsWith("（")) {
          val (name, step) = line.splitAt(line.indexWhere(c => c == ')' || c == '）') + 1)
          Some(name.substring(1, name.length - 1)) -> step
        } else None -> line
      })
      .zipWithIndex
      .par
      .filterNot { case ((_, line), _) => line.startsWith("#") || line.isEmpty }
      .map { case ((name, step), idx) => parseSSDL(step, file.pathAsString, idx + 1, chatInfo, name) }
      .seq
  }

  /**
   * 解析 SSDL
   *
   * @param string   待解析字符串
   * @param filepath 文件路径
   * @param lineId   行号
   * @param chatInfo 会话信息（针对非全局步骤）
   * @return 解析结果
   */
  private def parseSSDL(string: String, filepath: String, lineId: Long, chatInfo: ChatInfo, name: Option[String]): Either[ParseFailResult, ParseSuccessResult] = {
    Try(PatternParser.parseBasePattern(string)) match {
      case Failure(exception) => Left(ParseFailResult(lineId, filepath, exception.getMessage))
      case Success(result) => Right(ParseSuccessResult(lineId, filepath, result, chatInfo, name))
    }
  }
}
