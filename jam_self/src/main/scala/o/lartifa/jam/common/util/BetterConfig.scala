package o.lartifa.jam.common.util

import com.typesafe.config.Config

import scala.jdk.CollectionConverters.*

/**
 * 让 typesafe 配置更加好用
 *
 * Author: sinar
 * 2021/11/15 23:47
 */
object BetterConfig {
  implicit class ConfigHelper(config: Config) {
    /**
     * 获取字符串或使用默认值
     *
     * @param path 配置路径
     * @param default 默认值
     * @return 字符串
     */
    def getString(path: String, default: String): String =
      if (config.hasPath(path)) config.getString(path) else default

    /**
     * 获取整数或使用默认值
     *
     * @param path 配置路径
     * @param default 默认值
     * @return 整数
     */
    def getInt(path: String, default: Int): Int =
      if (config.hasPath(path)) config.getInt(path) else default

    /**
     * 获取长整数或使用默认值
     *
     * @param path 配置路径
     * @param default 默认值
     * @return 长整数
     */
    def getLong(path: String, default: Long): Long =
      if (config.hasPath(path)) config.getLong(path) else default

    /**
     * 获取布尔值或使用默认值
     *
     * @param path 配置路径
     * @param default 默认值
     * @return 布尔值
     */
    def getBoolean(path: String, default: Boolean): Boolean =
      if (config.hasPath(path)) config.getBoolean(path) else default

    /**
     * 获取字符串列表或使用默认值
     *
     * @param path 配置路径
     * @param default 默认值
     * @return 字符串列表
     */
    def getStringList(path: String, default: List[String]): List[String] =
      if (config.hasPath(path)) config.getStringList(path).asScala.toList else default

    /**
     * 获取整数列表或使用默认值
     *
     * @param path 配置路径
     * @param default 默认值
     * @return 整数列表
     */
    def getIntList(path: String, default: List[Int]): List[Int] =
      if (config.hasPath(path)) config.getIntList(path).asScala.map(_.toInt).toList else default

    /**
     * 获取长整数列表或使用默认值
     *
     * @param path 配置路径
     * @param default 默认值
     * @return 长整数列表
     */
    def getLongList(path: String, default: List[Long]): List[Long] =
      if (config.hasPath(path)) config.getLongList(path).asScala.map(_.toLong).toList else default
  }
}
