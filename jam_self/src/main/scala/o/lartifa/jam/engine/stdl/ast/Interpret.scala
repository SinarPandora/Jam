package o.lartifa.jam.engine.stdl.ast

/**
 * 解释 DTExp
 * Author: sinar
 * 2021/1/1 14:24
 */
trait Interpret[T] {
  def interpret(exp: T): String
}
