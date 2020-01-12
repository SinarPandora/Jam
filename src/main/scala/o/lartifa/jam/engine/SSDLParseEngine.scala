package o.lartifa.jam.engine

import java.nio.charset.Charset

import ammonite.ops._
import better.files.File
import o.lartifa.jam.common.config.SystemConfig
import o.lartifa.jam.engine.parser.PatternParser
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
object SSDLParseEngine {

  sealed case class ParseSuccessResult(lineId: Long, filepath: String, result: ParseResult)

  sealed case class ParseFailResult(lineId: Long, filepath: String, message: String)

  implicit val charset: Charset = Charset.forName("UTF-8")

  /**
   * 加载并解析 SSDL
   *
   * @param exec 异步执行上下文
   * @return 解析结果，键为 false 对应的内容为解析失败的信息
   */
  def load()(implicit exec: ExecutionContext): Future[Map[Boolean, Seq[Either[ParseFailResult, ParseSuccessResult]]]] = Future {
    loadFiles() |> parseFiles
  }

  /**
   * 加载 SSDL 文件
   *
   * @return 文件列表
   */
  private def loadFiles(): List[File] = {
    import SystemConfig._
    File(ssdlPath).listRecursively.filter(file => ssdlFileExtension.contains(file.extension.getOrElse(""))).toList
  }

  /**
   * 并行解析文本内容
   *
   * @param ssdlFiles 文件列表
   * @return 解析结果
   */
  private def parseFiles(ssdlFiles: List[File]): Map[Boolean, Seq[Either[ParseFailResult, ParseSuccessResult]]] = {
    ssdlFiles.flatMap(parseFileContent).groupBy(_.isRight)
  }

  /**
   * 解析文件内容
   *
   * @param file 文件对象
   * @return 解析结果
   */
  private def parseFileContent(file: File): Iterable[Either[ParseFailResult, ParseSuccessResult]] = {
    file.lines
      .map(_.trim)
      .zipWithIndex
      .par
      .filterNot(_._1.startsWith("#"))
      .map(pair => {
        parseSSDL(pair._1, file.pathAsString, pair._2 + 1)
      })
      .seq
  }

  /**
   * 解析 SSDL
   *
   * @param string   待解析字符串
   * @param filepath 文件路径
   * @param lineId   行号
   * @return 解析结果
   */
  private def parseSSDL(string: String, filepath: String, lineId: Long): Either[ParseFailResult, ParseSuccessResult] = {
    Try(PatternParser.parseBasePattern(string)) match {
      case Failure(exception) => ParseFailResult(lineId, filepath, exception.getMessage) |> Left.apply
      case Success(result) => ParseSuccessResult(lineId, filepath, result) |> Right.apply
    }
  }
}
