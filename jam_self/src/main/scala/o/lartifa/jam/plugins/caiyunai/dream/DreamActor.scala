package o.lartifa.jam.plugins.caiyunai.dream

import akka.actor.{Actor, ActorRef}
import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.plugins.caiyunai.dream.DreamActor.{ContentUpdated, Data, MenuEvent, WritingEvent}
import o.lartifa.jam.plugins.caiyunai.dream.DreamClient.Dream
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

  import context.become

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
   *
   * @param data 数据
   */
  def writing(data: Data): Receive = {
    case msg: WritingEvent =>
      msg.evt.message match {
        case text if text.startsWith("=") =>
          val newContent = text.stripPrefix("=").trim
          become {
            writing {
              data.copy(
                lastContent = data.content,
                content = newContent
              )
            }
          }
          reply(msg.evt, "内容已覆盖")
          sender() ! ContentUpdated(self, newContent, isAppend = false)
        case text if text.startsWith("+") =>
          val append = text.stripPrefix("+").trim
          become {
            writing {
              data.copy(
                lastContent = data.content,
                content = data.content + append
              )
            }
          }
          reply(msg.evt, "内容已追加")
          sender() ! ContentUpdated(self, append, isAppend = true)
        case text if text.stripPrefix("-").trim == "帮助" =>
          reply(msg.evt,
            """编写模式：
              |① 追加内容：发送 + 开头的消息：
              |  +追加内容
              |② 覆盖已编写内容：发送 = 开头的消息：
              |  =替换文本
              |③ 查看当前文章：-列出全文
              |④ 开启 AI 梦境（智能联想）： -入梦
              |⑤ 修改当前 AI 角色或文章标题： -修改角色/-修改标题
              |⑥ 退出：-退出""".stripMargin)
        case _ => menu(data, msg.toMenuEvent)
      }
    case evt: MenuEvent => menu(data, evt)
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
  def dreaming(data: Data, xid: String, dreams: List[Dream] = Nil): Receive = {
    case msg: WritingEvent =>
      val text = msg.evt.message
      if (text.stripPrefix("-").trim == "帮助") {
        reply(msg.evt,
          """梦境实现模式：
            |① 采用这条梦境：发送 +梦境编号：
            |  +2
            |② 换一批梦境： -再入梦
            |③ 查看当前文章：-列出全文
            |④ 修改当前 AI 角色或文章标题： -修改角色/-修改标题
            |⑤ 退出：-退出""".stripMargin)
      } else if (text.startsWith("+")) {
        Try(text.stripPrefix("+").trim.toInt - 1) match {
          case Success(idx) =>
            if (idx < 0 || idx > dreams.size - 1) {
              reply(msg.evt,
                s"指定的梦境不存在，可用梦境：${(1 to dreams.size).mkString("，")}")
            } else {
              become(skipping())
              Future {
                val dream = dreams(idx)
                DreamClient.realizingDream(data.uid, xid, idx) match {
                  case Left(errorMessage) | Right(false) => reply(msg.evt, errorMessage)
                  case Right(true) =>
                    reply(msg.evt, "梦境变成了现实")
                    become {
                      writing {
                        data.copy(
                          lastContent = data.content,
                          content = data.content + dream.content
                        )
                      }
                    }
                }
              }
            }
          case Failure(_) => reply(msg.evt, "请输入正确的编号")
        }
      }
    case evt: MenuEvent => menu(data, evt)
  }

  /**
   * 状态：更新标题
   * 交互：
   * - =内容：更新标题
   * - -返回：退出更改
   * - -帮助：输出帮助信息
   */
  def changingTitle(data: Data): Receive = ???

  /**
   * 状态：更改小梦角色
   * 交互：
   * - =1，=2，=3 选择角色
   * - -返回：退出更改
   * - -帮助：输出帮助信息
   */
  def changingAICharacter(data: Data): Receive = ???

  /**
   * 状态：操作中，跳过一切请求
   */
  def skipping(): Receive = ???

  /**
   * 菜单：
   * 以英文减号开头作为指令
   * - 修改标题：进入修改标题模式
   * - 修改角色：进入修改角色模式
   * - 列出全文：输出当前全文
   * - 开始联想：启动 AI 联想
   * - 换一批：与开始联想相同
   * - 退出：退出彩云小梦模式
   */
  def menu(data: Data, evt: MenuEvent): Unit = {
    if (evt.evt.message.startsWith("-")) {
      val command = evt.evt.message.stripPrefix("-").trim
      command match {
        case "修改标题" =>
        case "修改角色" =>
        case "列出全文" =>
        case "入梦" | "再入梦" =>
        case "退出" =>
        case _ =>
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
    mid: Option[String],
    signature: String,
  )

  case class WritingEvent(sender: ActorRef, evt: EventMessage) {
    def toMenuEvent: MenuEvent = MenuEvent(sender, evt)
  }

  case class MenuEvent(sender: ActorRef, evt: EventMessage)

  /**
   * 内容更新事件
   * - 追加模式：delta = 追加
   * - 覆盖模式：delta = 全文
   * - 当会话结束时，向全部订阅者发送全文
   */
  case class ContentUpdated(sender: ActorRef, delta: String, isAppend: Boolean)

  case object Shutdown

}
