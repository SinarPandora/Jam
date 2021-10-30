package o.lartifa.jam.plugins

import com.typesafe.config.Config
import o.lartifa.jam.common.config.botConfigFile

/**
 * 反向复读图片
 *
 * Author: sinar
 * 2021/5/9 20:18
 */
package object filppic {
  private val config: Config = botConfigFile.getConfig("system.message_listener.pre_handle.flip_repeat_picture")
  val useFFMpeg: Boolean = config.getBoolean("use_ffmpeg")
  val ffmpegPath: String = config.getString("ffmpeg_path")
}
