package o.lartifa.jam.model.commands

import o.lartifa.jam.common.exception.VarNotFoundException
import o.lartifa.jam.model.{CommandExecuteContext, VarKey}

import scala.async.Async.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * Author: sinar
 * 2020/7/18 16:20
 */
case class RenderStrTemplate(template: String, varKeys: Seq[VarKey] = Seq()) extends Command[String] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[String] = async {
    if (isPlainString) template
    else {
      val vars: Seq[String] = await(Future.sequence(varKeys.map(x => x.query.map(x -> _))))
        .map { case (name, opt) => opt.getOrElse(throw VarNotFoundException(name)) }
      template.format(vars*)
    }
  }

  /**
   * 该模板是否只是一个普通的字符串
   */
  val isPlainString: Boolean = varKeys.isEmpty
}

object RenderStrTemplate {
  val Empty: RenderStrTemplate = RenderStrTemplate("")
}
