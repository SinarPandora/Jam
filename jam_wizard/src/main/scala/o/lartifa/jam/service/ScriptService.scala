package o.lartifa.jam.service

import better.files._
import o.lartifa.jam.common.util.ErrorMsg
import o.lartifa.jam.model.SXDLScript
import o.lartifa.jam.utils.FileUtil
import org.springframework.stereotype.Service

import scala.util.Try

/**
 * 脚本服务
 *
 * Author: sinar
 * 2021/1/23 10:54
 */
@Service
class ScriptService {
  def saveOrCreate(script: SXDLScript): Try[Unit] = Try {
    val file = script.dir / script.content / script.ext
    file.createIfNotExists(createParents = true)
    file.writeText(script.content)
  }
  
  def getContent(path: String): Either[String, String] = {
    val script = path.toFile
    if (script.notExists) Left(ErrorMsg.IO.FILE_NOT_EXIST)
    else Right(path.toFile.contentAsString)
  }
  
  def delete(path: String, isDir: Boolean = false): Option[String] = {
    // TODO check if it is root dir of any instance
    val file = path.toFile
    if (file.notExists) Some(if (isDir) ErrorMsg.IO.DIR_NOT_EXIST else ErrorMsg.IO.FILE_NOT_EXIST)
    else {
      file.delete()
      None
    }
  }
}
