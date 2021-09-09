package o.lartifa.jam.model.commands

import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 交互式指令快捷方式
 *
 * Author: sinar
 * 2021/8/24 23:33
 */
case class ShellLikeShortcut[T](prefixes: Set[String], command: Command[T], templateStr: String, helpText: Option[String]) extends Command[T] {
  val argCount: Int = templateStr.count(_ == '%')

  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[T] = {
    val msg = context.eventMessage.message
    val content = prefixes.find(msg.startsWith).map(msg.stripPrefix).getOrElse(msg)
    val args = content.split("\\W+").toList
    val diff = argCount - args.size
    context.eventMessage.setMessage {
      if (diff > 0) {
        // 需要参数比实际多
        val strArgs = args ++ (1 to diff).map(_ => "")
        templateStr.format(strArgs *)
      } else if (diff < 0) {
        // 实际参数比需要多
        val additionParams = args.drop(argCount).mkString(" ")
        (if (templateStr.endsWith(" ")) {
          s"$templateStr$additionParams"
        } else {
          s"$templateStr $additionParams"
        }).format(args.take(argCount) *)
      } else {
        // 参数数量相等
        templateStr.format(args *)
      }
    }
    command.execute()
  }
}
