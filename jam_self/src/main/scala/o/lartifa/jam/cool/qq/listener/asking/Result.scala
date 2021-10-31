package o.lartifa.jam.cool.qq.listener.asking

/**
 * 问答结果
 *
 * Author: sinar
 * 2020/9/18 21:46
 */
@deprecated(since = "3.1", message = "Please use o.lartifa.jam.cool.qq.listener.interactive")
sealed abstract class Result

object Result {

  /**
   * 保持计数不变并继续提问该问题
   */
  case object KeepCountAndContinueAsking extends Result

  /**
   * 继续提问该问题
   */
  case object ContinueAsking extends Result

  /**
   * 稍后再问（将问题放在队列末尾，不减少计数）
   */
  case object AskAgainLater extends Result

  /**
   * 完成提问
   */
  case object Complete extends Result

  /**
   * 完成提问，并立刻继续向下处理此条消息
   */
  case object CompleteThenParseMessage extends Result

}
