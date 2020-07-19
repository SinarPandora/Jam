package o.lartifa.jam.engine.parser

import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.model.VarKey
import o.lartifa.jam.model.commands.RenderStrTemplate

/**
 * 解析引擎上下文
 *
 * Author: sinar
 * 2020/7/19 18:06
 */
case class ParseEngineContext(stepId: Long,
                              private val varKeys: Map[String, VarKey],
                              private val templates: Map[String, RenderStrTemplate],
                              rawStr: String, processedStr: String) {
  /**
   * 取当前步骤中的变量
   *
   * @param idKey 标识
   * @return 变量键
   */
  def getVar(idKey: String): VarKey = varKeys.getOrElse(idKey, throw ParseFailException("试图获取一个步骤中不存在的变量，这可能是一个 BUG"))

  /**
   * 取当前步骤中的字符串模板
   *
   * @param idKey 标识
   * @return 渲染模板指令
   */
  def getTemplate(idKey: String): RenderStrTemplate = templates.getOrElse(idKey, throw ParseFailException("试图获取一个步骤中不存在的模板，这可能是一个 BUG"))
}
