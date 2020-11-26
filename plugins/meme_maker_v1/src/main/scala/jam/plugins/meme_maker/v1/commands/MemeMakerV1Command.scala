package jam.plugins.meme_maker.v1.commands

import cc.moecraft.icq.event.events.message.EventMessage
import jam.plugins.meme_maker.v1.engine.MemeAPIV1Response.TemplatePair
import jam.plugins.meme_maker.v1.engine.MemeMakerAPI
import o.lartifa.jam.cool.qq.listener.asking.{Answerer, Result}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Command

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * Meme maker 指令
 *
 * Author: sinar
 * 2020/11/18 22:19
 */
object MemeMakerV1Command extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = {
    val templates = MemeMakerAPI.allTemplates
    if (templates.nonEmpty) {
      context.eventMessage.respond(
        """已启用表情动图制作工具，
          |请选择你想制作的模板编号
          |---------------------
          |输入"上一页"，"下一页"进行翻页
          |输入"预览"加上模板编号查看示例
          |输入模板编号开始制作
          |输入"退出"结束制作""".stripMargin)
      step1SelectTemplate(templates, 1, math.ceil(templates.size / 6).toInt)
    } else {
      context.eventMessage.respond("模板尚未准备好，请稍后重试")
    }
    Future.unit
  }

  /**
   * 第一步，选择模板（一页十条）
   *
   * @param templates 全部模板
   * @param page      当前页数
   * @param total     总页数
   * @param context   执行上下文
   * @param exec      异步上下文
   */
  private def step1SelectTemplate(templates: List[TemplatePair], page: Int, total: Int)(implicit context: CommandExecuteContext, exec: ExecutionContext): Unit = {
    val info = templates.slice((page - 1) * 6, page * 6).map { it =>
      import it._
      s"$id：$name"
    }.mkString("\n")

    context.eventMessage.respond(info)

    Answerer.sender ? { ctx =>
      ctx.event.message match {
        case "退出" =>
          respond("已退出")
          Future.successful(Result.Complete)
        case "上一页" =>
          if (page == 1) {
            respond("已经是第一页啦")
            step1SelectTemplate(templates, 1, total)
            Future.successful(Result.Complete)
          } else {
            step1SelectTemplate(templates, page - 1, total)
            Future.successful(Result.Complete)
          }
        case "下一页" =>
          if (page == total) {
            respond("已经是最后一页啦")
            Future.successful(Result.KeepCountAndContinueAsking)
          } else {
            step1SelectTemplate(templates, page + 1, total)
            Future.successful(Result.Complete)
          }
        case msg if msg.startsWith("预览") =>
          Try(msg.stripPrefix("预览").trim.toLong) match {
            case Failure(_) =>
              respond("请输入正确的序号！")
              Future.successful(Result.KeepCountAndContinueAsking)
            case Success(id) =>
              templates.find(_.id == id) match {
                case Some(template) => previewTemplate(template.code, ctx.event)
                case None =>
                  respond("没有该模板！")
                  Future.successful(Result.KeepCountAndContinueAsking)
              }
          }
        case other =>
          Try(other.toLong) match {
            case Failure(_) =>
              respond("请输入正确的序号！")
              Future.successful(Result.KeepCountAndContinueAsking)
            case Success(id) =>
              templates.find(_.id == id) match {
                case Some(template) =>
                  ctx.event.respond(
                    s"""已选择模板${template.name}，
                       |开始填充模板内容
                       |---------------------
                       |发送消息填充模板
                       |输入"=预览"加上模板编号查看示例
                       |输入"=退出"结束制作""".stripMargin)
                  fillTemplate(template)
                  Future.successful(Result.Complete)
                case None =>
                  respond("没有该模板！")
                  Future.successful(Result.KeepCountAndContinueAsking)
              }
          }
      }
    }
  }

  /**
   * 填充当前模板
   *
   * @param templateInfo 模板信息
   * @param context      执行上下文
   * @param exec         异步上下文
   */
  private def fillTemplate(templateInfo: TemplatePair)(implicit context: CommandExecuteContext, exec: ExecutionContext): Unit = {
    val slots = MemeMakerAPI.getTemplateSlots(templateInfo.code) match {
      case Failure(_) =>
        respond("获取模板信息失败")
        return
      case Success(value) => value
    }
    val sentences: ListBuffer[String] = ListBuffer.empty
    respond(
      s"""请填写第${sentences.size + 1}条句子
         |---------------------
         |示例：${slots(sentences.size)}""".stripMargin)
    Answerer.sender ? { ctx =>
      ctx.event.message match {
        case "=退出" =>
          respond("已退出")
          Future.successful(Result.Complete)
        case "=预览" => previewTemplate(templateInfo.code, ctx.event)
        case sentence =>
          sentences += sentence
          if (sentences.sizeIs == slots.size) {
            respond("填充完毕！正在生成...")
            Try(respond(
              MemeMakerAPI
                .generate(templateInfo.code, sentences.toList, ctx.event)
                .map(_.toString)
                .getOrElse("生成失败，请稍后重试")))
            Future.successful(Result.Complete)
          } else {
            respond(
              s"""请填写第${sentences.size + 1}条句子
                 |---------------------
                 |示例：${slots(sentences.size)}""".stripMargin)
            Future.successful(Result.KeepCountAndContinueAsking)
          }
      }
    }
  }

  /**
   * 预览插件
   *
   * @param code  插件地址
   * @param event 消息对象
   * @return 执行结果
   */
  private def previewTemplate(code: String, event: EventMessage): Future[Result] = {
    event.respond(s"预览地址：${MemeMakerAPI.domain}/$code")
    Future.successful(Result.KeepCountAndContinueAsking)
  }
}
