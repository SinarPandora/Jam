package o.lartifa.jam.common.exception

/**
 * 解析异常
 * Author: sinar
 * 2020/1/3 21:33 
 */
case class ParseFailException(message: String) extends Exception(message)
