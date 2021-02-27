package o.lartifa.jam.utils

import better.files._

import java.io.File

/**
 * Git 辅助工具
 *
 * Author: sinar
 * 2021/2/27 22:16
 */
object GitUtil {

  /**
   * 创建 ignore 文件
   *
   * @param rootDir 根目录
   * @return 操作结果
   */
  def createIgnoreFile(rootDir: File): Unit = {
    val ignoreFile = (rootDir.toScala / ".gitignore").createIfNotExists()
    ignoreFile.appendLines("# 以下类型的文件会被忽略", ".DS_Store", ".AppleDouble", ".LSOverride", "Icon", "._*",
      ".DocumentRevisions-V100", ".fseventsd", ".Spotlight-V100", ".TemporaryItems", ".Trashes",
      ".VolumeIcon.icns", ".com.apple.timemachine.donotpresent", ".AppleDB", ".AppleDesktop",
      "Network Trash Folder", "Temporary Items", ".apdisk", "*.bak", "*.gho", "*.ori", "*.orig",
      "*.tmp", "Thumbs.db", "Thumbs.db:encryptable", "ehthumbs.db", "ehthumbs_vista.db", "*.stackdump",
      "[Dd]esktop.ini", "$RECYCLE.BIN/", "*.cab", "*.msi", "*.msix", "*.msm", "*.msp", "*.lnk")
  }
}
