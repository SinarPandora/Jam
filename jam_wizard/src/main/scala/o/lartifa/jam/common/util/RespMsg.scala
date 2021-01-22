package o.lartifa.jam.common.util

/**
 * 返回信息工具
 *
 * Author: sinar
 * 2021/1/23 02:02
 */
object RespMsg {
  /**
   * 错误信息
   *
   * @param msg 详情
   * @return 响应体
   */
  def error(msg: String): Map[String, String] = Map("status" -> "fail", "detail" -> msg)

  /**
   * 返回内容
   *
   * @param status 状态
   * @param body   响应体数据
   * @return 响应体
   */
  def done(status: String, body: Any): Map[String, Any] = Map("status" -> status, "data" -> body)

  /**
   * 返回内容
   *
   * @param body 响应体数据
   * @return 响应体
   */
  def done(body: Any): Map[String, Any] = Map("status" -> "ok", "data" -> body)
}
