package o.lartifa.jam.plugins.story_runner

import o.lartifa.jam.common.config.JSONConfig.formats
import org.json4s.*
import org.json4s.jackson.JsonMethods.*
import org.json4s.jackson.Serialization.{read as jRead, write as jWrite}

/**
 * 故事存档
 *
 * Author: sinar
 * 2021/2/6 15:43
 */
case class SaveFile
(
  name: String,
  playerName: String,
  data: Option[String],
  choices: String,
  create_date: Long
) {
  def choicesAsList: List[String] = choices.split(",").toList

  def dataAsMap: Option[Map[String, String]] = data.map(data => parse(data).extract[Map[String, String]])
}

object SaveFile {
  def apply(name: String, playerName: String, data: Option[String], choices: String, create_date: Long): SaveFile = new SaveFile(name, playerName, data, choices, create_date)

  def apply(name: String, playerName: String, data: Map[String, String], choices: List[Int], create_date: Long): SaveFile = {
    new SaveFile(name, playerName, if (data.isEmpty) None else Some(jWrite(data)), choices.mkString(","), create_date)
  }


  /**
   * 从字符串读取存档
   *
   * @param string 字符串
   * @return 存档
   */
  def read(string: String): SaveFile = jRead[SaveFile](string)

  /**
   * 从字符串读取存档列表
   *
   * @param string 字符串
   * @return 存档
   */
  def readAsList(string: String): List[SaveFile] = jRead[List[SaveFile]](string)

  /**
   * 将存档写入字符串
   *
   * @param saveFile 存档对象
   * @return 字符串
   */
  def write(saveFile: SaveFile): String = jWrite[SaveFile](saveFile)

  /**
   * 将存档列表写入字符串
   *
   * @param saveFiles 存档列表
   * @return 字符串
   */
  def writeList(saveFiles: List[SaveFile]): String = jWrite[List[SaveFile]](saveFiles)
}

