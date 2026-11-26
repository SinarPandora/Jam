package o.lartifa.jam.plugins.nv_xing_jiao_liu

import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum
import com.github.houbb.pinyin.util.PinyinHelper

/** 敏感词杀手
  *
  * Author: sinar 2024/11/24 21:31
  */
object SensitiveWordKiller {
  private val RawWordPairs = Set(
    "肏屄",
    "蛋蛋",
    "高潮",
    "睾丸",
    "鸡巴",
    "鸡儿",
    "鸡鸡",
    "鸡掰",
    "奶子",
    "牛子",
    "penis",
    "jiba"
  )

  private val WordPairs: List[(String, List[String], String)] = RawWordPairs
    .map(it =>
      PinyinHelper.toPinyin(it, PinyinStyleEnum.NUM_LAST) ->
        PinyinHelper
          .toPinyin(it, PinyinStyleEnum.NORMAL)
          .replaceAll(" ", "")
          .toUpperCase
    )
    // 补充关键词
    .toSeq
    .sortBy(_._1)
    .map(it => (it._1, it._1.split(" ").toList, it._2))
    .toList

  private val PunctuationRegex = raw"""[.,;:!?'\"()\\[\\]{}<>《》（）【】「」『』、，。？！]"""

  /** 滑动窗口寻找对应屏蔽词，并返回查找结果
    *
    * @param rawText 聊天文本
    * @return 查找结果
    */
  def matching(rawText: String): List[String] = {
    if (rawText.isBlank) return Nil
    val pinyins: List[String] = PinyinHelper
      .toPinyin(
        rawText.replaceAll(PunctuationRegex, ""),
        PinyinStyleEnum.NUM_LAST
      )
      .split("\\s+")
      .toList :+ "_" :+ "_"
    pinyins
      .sliding(3)
      .flatMap { case first :: second :: third :: Nil =>
        val lowerBound = first.charAt(0).toString
        (if (lowerBound == "j" && jibaCheck(first, second)) List("JIBA")
         else List.empty) ++
          WordPairs
            .dropWhile(it => !it._1.startsWith(lowerBound))
            .takeWhile(it => it._1.startsWith(lowerBound))
            .filter { case (_, tones, _) =>
              tones match {
                case k1 :: Nil       => first.equalsIgnoreCase(k1)
                case k1 :: k2 :: Nil =>
                  first.equalsIgnoreCase(k1) && second.equalsIgnoreCase(k2)
                case k1 :: k2 :: k3 :: Nil =>
                  first.equalsIgnoreCase(k1) && second.equalsIgnoreCase(
                    k2
                  ) && third.equalsIgnoreCase(k3)
                case _ => false
              }
            }
            .map(_._3)
      }
      .distinct
      .map(it => s"【$it】")
      .toList
  }

  private def jibaCheck(fst: String, snd: String): Boolean = {
    "ji" == fst.substring(0, fst.length - 1) && "ba" == snd.substring(
      0,
      snd.length - 1
    )
  }
}
