package o.lartifa.jam.plugins

import o.lartifa.jam.common.config.PluginConfig

/**
 * 反向复读图片
 *
 * Author: sinar
 * 2021/5/9 20:18
 */
package object filppic {
  def useFFMpeg: Boolean = PluginConfig.config.preHandle.flipRepeatPicture.useFFMpeg
  def ffmpegPath: String = PluginConfig.config.preHandle.flipRepeatPicture.ffmpegPath
  def useRandomFilter: Boolean = PluginConfig.config.preHandle.flipRepeatPicture.useRandomFilter
}
