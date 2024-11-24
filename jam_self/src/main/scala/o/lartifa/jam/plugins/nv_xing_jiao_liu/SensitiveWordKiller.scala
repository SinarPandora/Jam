package o.lartifa.jam.plugins.nv_xing_jiao_liu

import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum
import com.github.houbb.pinyin.util.PinyinHelper

/**
 * 敏感词杀手
 *
 * Author: sinar
 * 2024/11/24 21:31
 */
object SensitiveWordKiller {
  private val WordPairs: Set[String] = Set(
    "AI YE", "BAI HU", "BAO JU", "ВАО РІ",
    "BAO YU", "BIAN TAI", "BO QI", "CAO BI", "CHAO CHUI",
    "CHA RU", "CHEN BO", "CHI HAN", "CHU NAN", "CHU NV",
    "DA BIAN", "DA BO", "DA ITAO", "DAN DAN", "DA PAO",
    "FANG SHI", "FA QING", "GANG JIAO", "GANG MEN",
    "GAO CHAO", "GAO WAN", "G DIAN", "GUAN CHANG",
    "HAI MIAN TI", "HOU TING", "HUA JING", "HUANG PIAN",
    "HUANG SE", "JIAO PEI", "JIAO WEI", "JI BA", "JI ER",
    "JI JI", "JI KE", "JING YE", "JING ZI", "JI TU",
    "JU DIAO", "JU RU", "KOU BAO", "KOU HUO", "KOU JIAO",
    "KOU QIU", "LIAN ZU", "LU CHU", "LU DIAN", "LUO NV",
    "LUO TI", "LU YIN", "MAI YIN", "MEI YAO", "MI MI",
    "NAI TOU", "NAI ZI", "NEI KU", "NEI SHE",
    "PAI XIE", "PANG CI", "PA PA PA", "PENIS", "PO CHU",
    "QIAN CAO", "QIAN LIE XIAN", "QI CHENG WEI", "QING SE",
    "QUAN JIAO",
  )

  private val WordMatchGroups: Set[List[String]] = WordPairs.map(_.split(" ").toList)
  private val PunctuationRegex = "\"[.,;:!?'\"()\\[\\]{}<>《》（）【】「」『』、，。？！]\""

  /**
   * 滑动窗口寻找对应屏蔽词，并返回查找结果
   *
   * @param rawText 聊天文本
   * @return 查找结果
   */
  def matching(rawText: String): List[String] = {
    if (rawText.isBlank) return Nil
    val pinyins: List[String] = PinyinHelper.toPinyin(
        rawText.replaceAll(PunctuationRegex, ""),
        PinyinStyleEnum.NORMAL)
      .split("\\s+")
      .toList :+ "_" :+ "_"
    pinyins.sliding(3).flatMap {
      case first :: second :: third :: Nil =>
        WordMatchGroups.filter {
          case k1 :: Nil => first.equalsIgnoreCase(k1)
          case k1 :: k2 :: Nil => first.equalsIgnoreCase(k1) && second.equalsIgnoreCase(k2)
          case k1 :: k2 :: k3 :: Nil => first.equalsIgnoreCase(k1) && second.equalsIgnoreCase(k2) && third.equalsIgnoreCase(k3)
          case _ => false
        }.flatten
    }.distinct.map(_.replaceAll("\\s+", "")).toList
  }
}
