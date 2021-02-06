package o.lartifa.jam.plugins.story_runner

import upickle.default.{macroRW, ReadWriter => RW, read => uRead, write => uWrite}

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

  def dataAsMap: Option[Map[String, String]] = data.map(data => ujson.read(data).obj.view.mapValues(_.str).toMap)
}

object SaveFile {
  def apply(name: String, playerName: String, data: Option[String], choices: String, create_date: Long): SaveFile = new SaveFile(name, playerName, data, choices, create_date)

  def apply(name: String, playerName: String, data: Map[String, String], choices: List[Int], create_date: Long): SaveFile = {
    new SaveFile(name, playerName, if (data.isEmpty) None else Some(uWrite(data)), choices.mkString(","), create_date)
  }

  implicit val rw: RW[SaveFile] = macroRW

  /**
   * 从字符串读取存档
   *
   * @param string 字符串
   * @return 存档
   */
  def read(string: String): SaveFile = uRead(string)

  /**
   * 从字符串读取存档列表
   *
   * @param string 字符串
   * @return 存档
   */
  def readAsList(string: String): List[SaveFile] = uRead(string)

  /**
   * 将存档写入字符串
   *
   * @param saveFile 存档对象
   * @return 字符串
   */
  def write(saveFile: SaveFile): String = uWrite(saveFile)

  /**
   * 将存档列表写入字符串
   *
   * @param saveFiles 存档列表
   * @return 字符串
   */
  def writeList(saveFiles: List[SaveFile]): String = uWrite(saveFiles)
}

