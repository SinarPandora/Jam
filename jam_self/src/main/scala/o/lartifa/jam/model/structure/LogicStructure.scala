package o.lartifa.jam.model.structure

import o.lartifa.jam.model.Executable
import o.lartifa.jam.model.behaviors.Breakable

/**
 * 结构原型
 *
 * Author: sinar
 * 2020/1/4 18:58
 */
abstract class LogicStructure extends Executable[Boolean] with Breakable
