package o.lartifa.jam.plugins.trpg.rule

import cc.moecraft.logger.HyLogger
import com.typesafe.config.Config
import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.pool.JamContext

import scala.jdk.CollectionConverters._
import scala.util.Try

/**
 * RPG 配置文件阅读器
 *
 * Author: sinar
 * 2021/7/31 20:12
 */
object TRPGRuleConfReader {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(TRPGRuleConfReader.getClass)
  private var defaultConf: Option[TRPGRuleConf] = None

  /**
   * 读取全部配置
   *
   * @param config 配置
   * @return 有效规则配置列表
   */
  @throws[ParseFailException]
  def readAll(config: Config): Map[String, TRPGRuleConf] = {
    this.defaultConf = None
    val ruleConfigs = config.getObject("规则")
    val defaultConfig: TRPGRuleConf = new SingleRuleConfReader("默认").read(ruleConfigs.atKey("默认"))
    this.defaultConf = Some(defaultConfig)
    val others = ruleConfigs.withoutKey("默认")
    val result = others.keySet().asScala
      .map(name => name -> new SingleRuleConfReader(name).read(others.atKey(name)))
      .toMap + ("默认" -> defaultConfig)
    this.defaultConf = None
    result
  }

  class SingleRuleConfReader(ruleName: String) {
    /**
     * 读取单个配置
     *
     * @param config 配置
     * @return 规则配置
     */
    @throws[ParseFailException]
    def read(config: Config): TRPGRuleConf = {
      try {
        val actorGeneration = {
          if (config.hasPath("人物生成")) {
            readActorGeneration(config.getConfig("人物生成"))
          } else defaultConf.getOrElse(failToRead("人物生成", """默认配置没有"人物生成"配置""")).actorGeneration
        }
        val checking = {
          if (config.hasPath("检定")) {
            readChecksConf(config.getConfig("检定"))
          } else defaultConf.getOrElse(failToRead("检定", """默认配置没有"检定"配置""")).checking
        }
        val extraAttrs: Map[String, ExtraAttr] = {
          if (config.hasPath("额外属性")) {
            val extraAttrs = config.getObject("额外属性")
            readExtraAttrs {
              extraAttrs.keySet().asScala
                .map(name => name -> extraAttrs.atKey(name))
                .toMap
            }
          } else Map.empty
        }
        val extraAdjusts = {
          if (config.hasPath("额外调整")) {
            val extraAdjusts = config.getObject("额外调整")
            readExtraAdjusts {
              extraAdjusts.keySet().asScala
                .map(name => name -> extraAdjusts.atKey(name))
                .toList
            }
          } else List.empty
        }
        TRPGRuleConf(
          name = ruleName,
          checking = checking,
          extraAttrs = extraAttrs,
          extraAdjusts = extraAdjusts,
          actorGeneration = actorGeneration
        )
      } catch {
        case e: Throwable =>
          logger.error(e)
          throw ParseFailException(s"解析规则${ruleName}时发生未知错误，请参照官方例子检查默认规则内容")
      }
    }

    /**
     * 抛出异常
     *
     * @param parent 错误父级字段位置
     * @param reason 错误原因
     */
    @throws[ParseFailException]
    private def failToRead[T](parent: String, reason: String): T = {
      throw ParseFailException(s"规则：${ruleName}读取失败，原因为：$reason，错误位置：$parent")
    }

    /**
     * 读取任务生成配置
     *
     * @param config 配置
     * @return 人物生成配置
     */
    @throws[ParseFailException]
    private def readActorGeneration(config: Config): ActorGeneration = {
      val ratio = {
        if (config.hasPath("整体倍率")) {
          Try(config.getInt("整体倍率"))
            .getOrElse(failToRead("人物生成", "整体倍率必须是数字"))
        } else defaultConf.getOrElse(failToRead("人物生成", "默认配置没有配置人物属性整体倍率"))
          .actorGeneration.ratio
      }
      val attrs = {
        if (config.hasPath("属性")) {
          val attrs = config.getObject("属性")
          attrs.keySet().asScala
            .map(name => name -> attrs.atKey(name))
            .map(params => readActorAttr(params._1, params._2))
            .toSeq
        } else defaultConf.getOrElse(failToRead("人物生成", "默认配置没有配置人物属性"))
          .actorGeneration.actorAttrs
      }
      ActorGeneration(ratio, attrs)
    }

    /**
     * 读取单个人物属性
     *
     * @param name   属性名
     * @param config 配置
     * @return 任务属性
     */
    @throws[ParseFailException]
    private def readActorAttr(name: String, config: Config): ActorAttr = {
      val valueExpr: String = {
        if (config.hasPath("数值")) {
          config.getString("数值").trim
        } else failToRead("属性", s"未给人物属性${name}设置数值表达式")
      }
      val range: String = {
        if (config.hasPath("范围")) {
          config.getString("范围").trim
        } else failToRead("属性", s"未给人物属性${name}设置范围")
      }
      val hidden: Option[Boolean] = {
        if (config.hasPath("隐藏")) {
          Some(config.getBoolean("隐藏"))
        } else None
      }
      ActorAttr(name, valueExpr, range, hidden)
    }

    /**
     * 读取检定规则
     *
     * @param config 规则
     * @return 检定规则
     */
    @throws[ParseFailException]
    private def readChecksConf(config: Config): CheckersConf = {
      val successRule = {
        if (config.hasPath("成功规则")) {
          val successRule = config.getString("成功规则").trim
          if (successRule != "小于" && successRule != "大于") {
            failToRead("检定", s"""成功规则必须在"大于"，"大于等于"，"小于"或"小于等于"中选择，当前值为$successRule""")
          } else successRule
        } else defaultConf.getOrElse(failToRead("检定", "默认配置没有配置检定判定成功规则"))
          .checking.successRule
      }
      val hugeSuccess = readSingleCheck("大成功", config.getConfig("大成功"), defaultConf.map(_.checking.hugeSuccess))
      val hugeFail = readSingleCheck("大失败", config.getConfig("大失败"), defaultConf.map(_.checking.hugeFail))
      val hardSuccess = readSingleCheck("困难成功", config.getConfig("困难成功"), defaultConf.map(_.checking.hardSuccess))
      val veryHardSuccess = readSingleCheck("极难成功", config.getConfig("极难成功"), defaultConf.map(_.checking.veryHardSuccess))
      CheckersConf(
        successRule = successRule,
        hugeSuccess = hugeSuccess,
        hugeFail = hugeFail,
        hardSuccess = hardSuccess,
        veryHardSuccess = veryHardSuccess
      )
    }

    /**
     * 解析单条检定规则
     *
     * @param name    规则名
     * @param config  配置
     * @param default 默认配置
     * @return 检定规则
     */
    @throws[ParseFailException]
    private def readSingleCheck(name: String, config: Config, default: Option[CheckConf]): CheckConf = {
      val range = if (config.hasPath("条件")) {
        config.getString(name)
      } else default.map(_.range).getOrElse(failToRead("检定", s"默认配置没有配置${name}条件"))
      val prompt = {
        if (config.hasPath(s"提示语概率") && config.hasPath(s"提示语")) {
          val prob = {
            if (config.hasPath("提示语概率")) {
              val prob = Try(config.getDouble("提示语概率"))
              if (prob.isFailure || prob.get > 1) {
                failToRead("检定", s"${name}的提示概率应为不超过1的小数")
              } else prob.get
            } else 0
          }
          val msgs = {
            if (config.hasPath("提示语")) {
              Try(config.getStringList("提示语").asScala.toSeq)
                .getOrElse(failToRead("检定", s"${name}的提示语列表格式有误"))
            } else Seq.empty
          }
          if (prob <= 0 || msgs.isEmpty) None
          else Some(CheckPrompt(prob, msgs))
        } else default.flatMap(_.prompt)
      }
      CheckConf(range, prompt)
    }

    /**
     * 读取额外属性
     *
     * @param configs 属性名 -> 配置
     * @return 全部额外属性
     */
    @throws[ParseFailException]
    private def readExtraAttrs(configs: Map[String, Config]): Map[String, ExtraAttr] = {
      if (configs.isEmpty) return Map.empty
      configs.view.map {
        case (name, config) =>
          val defaultValue = {
            if (config.hasPath("默认值")) {
              config.getString("默认值")
            } else failToRead("额外属性", s"额外属性${name}没有默认值")
          }
          val range = {
            if (config.hasPath("范围")) Some(config.getString("范围"))
            else None
          }
          val hidden = {
            if (config.hasPath("隐藏")) Some(config.getBoolean("隐藏"))
            else None
          }
          name -> ExtraAttr(name, defaultValue, hidden, range)
      }.toMap
    }

    /**
     * 读取额外调整
     *
     * @param configs 调整属性 -> 配置
     * @return 额外调整列表
     */
    @throws[ParseFailException]
    private def readExtraAdjusts(configs: List[(String, Config)]): List[ExtraAdjust] = {
      configs.map {
        case (name, config) =>
          val adjust = {
            if (config.hasPath("调整")) {
              config.getString("调整")
            } else failToRead("额外调整", s"未给属性${name}设置调整内容")
          }
          val range = {
            if (config.hasPath("范围")) Some(config.getString("范围")) else None
          }
          ExtraAdjust(name, adjust, range)
      }
    }
  }
}
