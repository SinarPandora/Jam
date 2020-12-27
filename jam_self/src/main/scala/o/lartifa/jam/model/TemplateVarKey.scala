package o.lartifa.jam.model

import cc.moecraft.icq.sender.message.components.{ComponentAt, ComponentFace, ComponentImage}
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.TemplateVarKey.TemplateVarSubType
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * 模板变量
 *
 * Author: sinar
 * 2020/12/26 23:22
 */
class TemplateVarKey(valueVarKey: VarKey, subType: TemplateVarSubType) extends VarKey("模板变量", valueVarKey.category) {
  /**
   * 获取变量
   *
   * @param context 执行上下文
   * @return 变量值（Optional）
   */
  override def query(implicit context: CommandExecuteContext): Future[Option[String]] = {
    implicit val exec: ExecutionContext = context.executionContext
    valueVarKey.query.map(_.getOrElse(valueVarKey.name)).map { value =>
      subType match {
        case TemplateVarKey.At =>
          val qId = Try(value.toLong).getOrElse(throw ExecutionException(s"At参数不是合法的QQ号$value"))
          Future.successful(new ComponentAt(qId))
        case TemplateVarKey.QQFace =>
          val fId = Try(value.toInt).getOrElse(throw ExecutionException(s"QQ小表情Id不合法$value"))
          Future.successful(new ComponentFace(fId))
        case TemplateVarKey.Image =>
          Future.successful(new ComponentImage(value))
        case TemplateVarKey.Command =>
          val stepId = Try(value.toLong).getOrElse(throw ExecutionException(s"指令编号不合法$value"))
          JamContext.stepPool.get().getById(stepId)
            .getOrElse(throw ExecutionException(s"编号为${stepId}的指令不存在"))
            .execute()
      }
    }.flatMap(x => x).map(it => Some(it.toString))
  }
}

object TemplateVarKey {

  sealed abstract class TemplateVarSubType(val prefixes: Set[String])

  // 艾特
  case object At extends TemplateVarSubType(Set("@"))

  // QQ 表情
  case object QQFace extends TemplateVarSubType(Set("#"))

  // 图片
  case object Image extends TemplateVarSubType(Set("图", "P"))

  // 指令
  case object Command extends TemplateVarSubType(Set("&"))

}
