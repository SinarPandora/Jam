package o.lartifa.jam.engine.parser

import o.lartifa.jam.model.VarKey
import o.lartifa.jam.model.commands.RenderStrTemplate

/**
 * 解析引擎上下文
 *
 * Author: sinar
 * 2020/7/19 18:06
 */
case class ParseEngineContext(stepId: Long, varKeys: Map[Int, VarKey], templates: Map[Int, RenderStrTemplate],
                              rawStr: String, processedStr: String)
