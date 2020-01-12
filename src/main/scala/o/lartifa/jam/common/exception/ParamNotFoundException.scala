package o.lartifa.jam.common.exception

/**
 * 变量缺失异常
 *
 * Author: sinar
 * 2020/1/4 16:25 
 */
case class ParamNotFoundException(paramName: String) extends Exception(s"没有找到名为：${paramName}的变量")
