package o.lartifa.jam.plugins.caiyunai.dream

import akka.actor.{Actor, ActorRef}
import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.plugins.caiyunai.dream.DreamActor.{ContentUpdated, Data, Event}
import o.lartifa.jam.plugins.caiyunai.dream.DreamClient.{AICharacter, Dream}
import requests.Session

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * 彩云小梦核心逻辑
 *
 * Author: sinar
 * 2021/2/20 20:26
 */
class DreamActor(startEvt: EventMessage) extends Actor {

  private val chatInfo: ChatInfo = ChatInfo(startEvt)

  private implicit val session: Session = requests.Session()

  import context.{become, unbecome}

  override def receive: Receive = ???

  /**
   * 状态：初始化
   *
   * @return
   */
  def init(): Receive = {
    reply(startEvt, s"首先一棒子打晕${JamConfig.name}……")
    ???
  }

  /**
   * 状态：写作中
   * 交互：
   * - 覆盖模式：=内容
   * - 追加模式：+内容
   * - -帮助：输出帮助信息
   */
  def writing(data: Data): Receive = {
    case msg: Event =>
      msg.evt.message match {
        case text if text.startsWith("=") =>
          val newContent = text.stripPrefix("=").trim
          become(skipping(data))
          Future {
            saveContent {
              data.copy(
                lastContent = data.content,
                content = newContent
              )
            } match {
              case Some(data) =>
                sender() ! ContentUpdated(self, newContent, isAppend = false)
                become(writing(data))
                reply(msg.evt, "内容已覆盖")
              case None => unbecome()
            }
          }
        case text if text.startsWith("+") =>
          val append = text.stripPrefix("+").trim
          become(skipping(data))
          Future {
            saveContent {
              data.copy(
                lastContent = data.content,
                content = data.content + append
              )
            } match {
              case Some(data) =>
                sender() ! ContentUpdated(self, append, isAppend = true)
                become(writing(data))
                reply(msg.evt, "内容已追加")
              case None => unbecome()
            }
          }
        case text if text.stripPrefix("-").trim == "帮助" =>
          reply(msg.evt,
            """编写模式：
              |① 追加内容：发送 + 开头的消息，如：
              |  +追加内容
              |② 覆盖已编写内容：发送 = 开头的消息，如：
              |  =替换文本
              |③ 查看当前文章：-全文
              |④ 开启 AI 梦境（智能联想）： -入梦
              |⑤ 修改当前 AI 角色或文章标题： -更改角色/-修改标题
              |⑥ 撤销本次更改：-撤回
              |⑦ 退出：-退出""".stripMargin)
        case _ => menu(data, msg)
      }
  }

  /**
   * 状态：AI 联想中
   * 交互：
   * - 联想过程中无法操作
   * - 联想完毕后输入：
   * --- +1，+2，+3 添加内容
   * - 输入-换一批 重新联想
   * - -返回：退出更改
   * - -帮助：输出帮助信息
   */
  def dreaming(data: Data, dreams: List[Dream] = Nil): Receive = {
    case msg: Event =>
      val text = msg.evt.message
      if (text.startsWith("+")) {
        Try(text.stripPrefix("+").trim.toInt - 1) match {
          case Success(idx) =>
            if (idx < 0 || idx > dreams.size - 1) {
              reply(msg.evt,
                s"指定的梦境不存在，可用梦境编号：${(1 to dreams.size).mkString("，")}")
            } else {
              become(skipping(data))
              Future {
                val dream = dreams(idx)
                DreamClient.realizingDream(data.uid, dream.xid, idx) match {
                  case Left(_) | Right(false) =>
                    reply(msg.evt, "梦境实体化失败，请稍后重试")
                    unbecome()
                  case Right(true) =>
                    val updatedData = data.copy(
                      lastContent = data.content,
                      content = data.content + dream.content
                    )
                    saveContent(updatedData) match {
                      case Some(data) =>
                        become(writing(data))
                        reply(msg.evt, "梦境变成了现实")
                      case None =>
                        // 即使保存失败也退回到编辑模式，因为此时梦境已经结束
                        become(writing(updatedData))
                    }
                }
              }
            }
          case Failure(_) => reply(msg.evt, "请输入正确的编号")
        }
      } else if (text.startsWith("-")) {
        val command = text.stripPrefix("-").trim
        if (command == "返回") {
          // 因为支持多重梦境，因此直接跳转回编辑模式
          become(writing(data))
          reply(msg.evt, "已返回编辑模式")
        } else if (command == "帮助") {
          reply(msg.evt,
            """梦境实现模式：
              |① 采用这条梦境：发送 +梦境编号，如：
              |  +2
              |② 换一批梦境： -再入梦
              |③ 查看当前文章：-全文
              |④ 修改当前 AI 角色或文章标题： -更改角色/-修改标题
              |   更改角色/标题会导致梦境中断
              |⑤ 返回编写模式：-返回""".stripMargin)
        } else menu(data, msg)
      }
  }

  /**
   * 状态：更新标题
   * 交互：
   * - =内容：更新标题
   * - -返回：退出更改
   * - -帮助：输出帮助信息
   */
  def changingTitle(data: Data): Receive = {
    case msg: Event =>
      val text = msg.evt.message
      if (text.startsWith("=")) {
        val updatedData = data.copy(
          title = text.stripPrefix("=").trim
        )
        become(skipping(data))
        saveContent(updatedData) match {
          case Some(data) =>
            become(writing(data))
            reply(msg.evt, "标题已更新")
          case None =>
            // 手动保存会同时保存标题，所以退回编辑模式
            become(writing(updatedData))
        }
      } else if (text.startsWith("-")) {
        val command = text.stripPrefix("-").trim
        if (command == "返回") {
          unbecome()
          reply(msg.evt, "已返回编辑模式")
        } else if (command == "帮助") {
          reply(msg.evt,
            """标题更改模式：
              |① 更改标题：发送 =新标题 进行更改，如：
              |  =新标题
              |② 返回：-返回""".stripMargin)
        } else menu(data, msg)
      }
  }

  /**
   * 状态：更改小梦角色
   * 交互：
   * - =1，=2，=3 选择角色
   * - -返回：退出更改
   * - -帮助：输出帮助信息
   */
  def changingAICharacter(data: Data): Receive = {
    case msg: Event =>
      val text = msg.evt.message
      if (text.startsWith("=")) {
        Try(text.stripPrefix("=").trim.toInt - 1) match {
          case Success(idx) =>
            if (idx < 0 || idx > data.models.size - 1) {
              reply(msg.evt,
                s"指定的角色不存在，可用角色编号：${(1 to data.models.size).mkString("，")}")
            } else {
              val model = data.models(idx)
              become {
                writing {
                  data.copy(mid = model.mid)
                }
              }
              reply(msg.evt, s"接下来的梦境将包括：${model.name} 元素")
            }
          case Failure(_) => reply(msg.evt, "请输入正确的编号")
        }
      } else if (text.startsWith("-")) {
        val command = text.stripPrefix("-").trim
        if (command == "返回") {
          unbecome()
          reply(msg.evt, "已返回编辑模式")
        } else if (command == "帮助") {
          reply(msg.evt,
            """角色更改模式：
              |① 更改角色：发送 =角色编号 进行更改，如：
              |  =3
              |② 返回：-返回""".stripMargin)
        } else menu(data, msg)
      }
  }

  /**
   * 状态：操作中，跳过一切请求
   */
  def skipping(data: Data): Receive = {
    case msg: Event =>
      val message = msg.evt.message
      if (message.stripPrefix("-").trim == "退出") {
        exit(data)
      } else if (message.startsWith("-") || message.startsWith("+") || message.startsWith("=")) {
        reply(msg.evt, "正在处理中，暂时无法进行操作……")
      }
  }

  /**
   * 菜单：
   * 以英文减号开头作为指令
   * - 修改标题：进入修改标题模式
   * - 更改角色：进入更改角色模式
   * - 列出全文：输出当前全文
   * - 开始联想：启动 AI 联想
   * - 换一批：与开始联想相同
   * - 退出：退出彩云小梦模式
   */
  def menu(data: Data, evt: Event): Unit = {
    if (evt.evt.message.startsWith("-")) {
      val command = evt.evt.message.stripPrefix("-").trim
      command match {
        case "修改标题" =>
          reply(evt.evt,
            s"""当前标题为：${if (data.title.trim == "") "未设置标题" else s"《${data.title.trim}》"}
              |发送 =新标题 来更改标题，如：=演员的自我修养
              |发送 -返回 取消并回到编辑模式""".stripMargin)
          become(changingTitle(data))
        case "更改角色" =>
          reply(evt.evt,
            s"""当前角色为：${data.models.find(_.mid == data.mid).map(_.name).getOrElse("未知")}
               |---------------------
               |可用角色如下：
               |${data.models.zipWithIndex.map { case (model, idx) => s"${idx + 1}：${model.name}" }.mkString("\n")}
               |发送 =编号 来更改 AI 角色，如：=2
               |发送 -返回 取消并回到编辑模式""".stripMargin)
          become(changingAICharacter(data))
        case "全文" => reply(evt.evt, "全文如下：\n" + data.content)
        case "入梦" | "再入梦" => dropIntoDream(data)
        case "撤回" =>
          become(writing(data.copy(content = data.lastContent)))
          reply(evt.evt, "更改已撤回，发送 -全文 查看当前全文")
        case "保存" =>
          become(skipping(data))
          Future {
            saveContent(data) match {
              case Some(data) =>
                become(writing(data))
                reply(evt.evt, "手动保存成功")
              case None =>
                reply(evt.evt, s"若一直无法保存，请尝试退出并重新进入梦境或通知${JamConfig.name}的监护人")
            }
          }
        case "退出" => exit(data)
        case _ => reply(evt.evt, s"指令：$command 不存在，你可以发送 -帮助 来获取帮助信息")
      }
    }
  }

  /**
   * 回复消息
   * - 每 200 个字符分块
   *
   * @param evt 事件
   * @param msg 消息内容
   */
  private def reply(evt: EventMessage, msg: String): Unit = {
    msg.sliding(200, 200).foreach(evt.respond)
  }

  /**
   * 保存当前文稿
   *
   * @param data 会话数据
   * @return 更新后的会话数据
   */
  private def saveContent(data: Data): Option[Data] = {
    data match {
      case Data(_, content, title, uid, nid, _, _, _) =>
        DreamClient.save(title, content, uid, nid) match {
          case Left(_) => startEvt.respond(
            """保存失败，你可以稍后尝试手动保存：
              |发送 -保存 进行保存""".stripMargin)
            None
          case Right(nid) => Some {
            data.copy(nid = Some(nid))
          }
        }
    }
  }

  /**
   * 坠梦（开启 AI 联想模式）
   *
   * @param data 会话数据
   * @return 更新后的会话数据
   */
  private def dropIntoDream(data: Data): Option[Data] = {
    ???
  }

  /**
   * 退出
   * 退出前将文章发送给原始会话和所有连接的聊天会话
   *
   * @param data 会话数据
   */
  private def exit(data: Data): Unit = {
    ???
  }
}

object DreamActor {

  /**
   * 数据
   * - 梦境相关数据属于临时数据，不保存在状态中
   */
  private case class Data
  (
    lastContent: String = "",
    content: String = "",
    title: String = "",
    uid: String,
    nid: Option[String],
    mid: String,
    signature: String,
    models: List[AICharacter]
  )

  case class Event(sender: ActorRef, evt: EventMessage)

  /**
   * 内容更新事件
   * - 追加模式：delta = 追加
   * - 覆盖模式：delta = 全文
   * - 当会话结束时，向全部订阅者发送全文
   */
  case class ContentUpdated(sender: ActorRef, delta: String, isAppend: Boolean)

  case object Shutdown

}
