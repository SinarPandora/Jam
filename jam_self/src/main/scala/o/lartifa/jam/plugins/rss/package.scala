package o.lartifa.jam.plugins

import com.apptastic.rssreader.{Item, RssReader}
import o.lartifa.jam.common.config.PluginConfig

import scala.util.matching.Regex

/**
 * Author: sinar
 * 2020/8/29 01:55
 */
package object rss {
  val rss: RssReader = new RssReader()
  val RSS_HUB_SOURCE: Regex = "(.+)/.+".r("category")

  def RssConfig: PluginConfig.Rss = PluginConfig.config.rss

  implicit class ItemHelper(item: Item) {
    /**
     * 获取 RSS 资源中可以用于比较的键
     *
     * @return 键值，当值为 NO_ANY_KEY 时代表比较键不存在
     */
    def key: String = item.getPubDate
      .orElse(item.getLink
        .orElse(item.getTitle
          .orElse("NO_ANY_KEY"
          )
        )
      )

    /**
     * RSS 元素是否包含可以用作比较的键
     *
     * @return 结果
     */
    def hasAnyKey: Boolean = key == "NO_ANY_KEY"
  }

}
