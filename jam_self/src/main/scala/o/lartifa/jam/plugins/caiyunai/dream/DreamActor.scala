package o.lartifa.jam.plugins.caiyunai.dream

import akka.actor.{Actor, ActorRef}
import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.plugins.caiyunai.dream.DreamActor._
import o.lartifa.jam.plugins.caiyunai.dream.DreamClient.{AICharacter, Dream}
import o.lartifa.jam.pool.JamContext
import requests.Session

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
 * 彩云小梦核心逻辑
 * 向 Actor 发送 Start 事件来启动它
 *
 * Author: sinar
 * 2021/2/20 20:26
 */
class DreamActor(startEvt: EventMessage) extends Actor {

  private implicit val session: Session = requests.Session()

  import context.{become, unbecome}

  /**
   * 状态：初始化
   */
  override def receive: Receive = {
    case Start(sender) => init(sender)
    case _ => // 默认什么也不做
  }


  /**
   * 初始化
   *
   * @param sender 消息发送者
   */
  def init(sender: ActorRef): Unit = {
    reply(startEvt,
      s"""启动坠梦程序，首先一棒子打晕${JamConfig.name}……
         |检测到目标意识模糊，正在连接意识探针……""".stripMargin)
    Future.sequence(Seq(
      Future(DreamClient.getUid),
      Future(DreamClient.listModels),
      Future(DreamClient.getSignature)
    )).flatMap {
      case Seq(_uid, _models, _signature) =>
        val uid = _uid.asInstanceOf[Either[String, String]]
        val models = _models.asInstanceOf[Either[String, List[AICharacter]]]
        val signature = _signature.asInstanceOf[Either[String, String]]
        if (uid.isLeft || models.isLeft || signature.isLeft || models.getOrElse(Nil).isEmpty) Future.successful(None)
        else {
          // 此处理应不能失败
          Future.successful(Some(Data(
            uid.toOption.get,
            models.toOption.get.head.mid,
            models.toOption.get,
            signature.toOption.get
          )))
        }
    }.map {
      case Some(data) =>
        reply(startEvt,
          """意识连接建立成功！
            |你可以发送 -帮助 打开帮助菜单
            |祝创作愉快~""".stripMargin)
        become(writing(data))
        sender ! Ready(self)
      case None =>
        sender ! FailToStart(sender)
        reply(startEvt, s"${JamConfig.name}拒绝了意识探针！目标已苏醒，坠梦失败。")
        context.stop(self)
    }.recoverWith(err => {
      sender ! FailToStart(sender)
      logger.error("彩云小梦模块初始化过程中出现未知错误！", err)
      reply(startEvt, s"${JamConfig.name}拒绝了意识探针！目标已苏醒，坠梦失败。")
      context.stop(self)
      Future.unit
    })
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
                msg.sender ! ContentUpdated(self, newContent, isAppend = false)
                become(writing(data))
                reply(msg.evt, "✅已覆盖")
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
                msg.sender ! ContentUpdated(self, append, isAppend = true)
                become(writing(data))
                reply(msg.evt, "✅已追加")
              case None => unbecome()
            }
          }
        case text if text.startsWith("-") && text.stripPrefix("-").trim == "帮助" =>
          reply(msg.evt,
            """编写模式帮助：
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
                        msg.sender ! ContentUpdated(self, dream.content, isAppend = true)
                        reply(msg.evt, "✅梦境变成了现实")
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
            """梦境实现模式帮助：
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
            msg.sender ! ContentUpdated(self, s"标题变更为：${data.title}", isAppend = true)
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
            """标题更改模式帮助：
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
              msg.sender ! ContentUpdated(self, s"梦境主题替换为${model.name}", isAppend = true)
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
            """角色更改模式帮助：
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
      if (message.stripPrefix("-").trim == "退出" && !data.isExiting) {
        exit(data.copy(isExiting = true), msg)
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
            s"""当前标题为：${if (data.title.isBlank) "未设置标题" else s"《${data.title.trim}》"}
               |发送 =新标题 来更改标题，如：=演员的自我修养
               |发送 -返回 取消并回到编辑模式
               |发送 -帮助 打开帮助菜单""".stripMargin)
          become(changingTitle(data))
        case "更改角色" =>
          reply(evt.evt,
            s"""当前角色为：${data.models.find(_.mid == data.mid).map(_.name).getOrElse("未知")}
               |---------------------
               |可用角色如下：
               |${data.models.zipWithIndex.map { case (model, idx) => s"${idx + 1}：${model.name}" }.mkString("\n")}
               |发送 =编号 来更改 AI 角色，如：=2
               |发送 -返回 取消并回到编辑模式
               |发送 -帮助 打开帮助菜单""".stripMargin)
          become(changingAICharacter(data))
        case "全文" => reply(evt.evt,
          s"""全文如下：
             |${if (data.title.isBlank) "未设置标题" else s"《${data.title.trim}》"}
             |${data.content}""".stripMargin)
        case "入梦" | "再入梦" => dropIntoDream(data)
        case "撤回" =>
          become(writing(data.copy(content = data.lastContent)))
          evt.sender ! ContentUpdated(self, "✅更改已撤回", isAppend = true)
          reply(evt.evt, "更改已撤回，发送 -全文 查看当前全文")
        case "保存" =>
          become(skipping(data))
          Future {
            saveContent(data) match {
              case Some(data) =>
                become(writing(data))
                reply(evt.evt, "✅手动保存成功")
              case None =>
                reply(evt.evt, s"若一直无法保存，请尝试退出并重新进入梦境或通知${JamConfig.name}的监护人")
            }
          }
        case "退出" => exit(data.copy(isExiting = true), evt)
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
    // TODO 给输出带一些延迟
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
      case Data(uid, _, _, _, title, nid, _, content, _) =>
        DreamClient.save(title, content, uid, nid) match {
          case Left(_) =>
            reply(startEvt,
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
  private def dropIntoDream(data: Data): Unit = {
    become(skipping(data))
    Future {
      if (data.nid.isEmpty || data.content.isBlank) {
        become(writing(data)) // 因为存在多重梦境，所以梦境被中断时，应强制退回编辑模式
        reply(startEvt, "巧妇难为无米之炊，先写点内容吧")
        None
      } else {
        DreamClient.dream(data.title, data.content, data.uid, data.nid.get, data.mid) match {
          case Right(xid) =>
            reply(startEvt, s"${JamConfig.name}成功入梦，正在记录梦境……")
            Some(xid)
          case Left(_) =>
            become(writing(data)) // 因为存在多重梦境，所以梦境被中断时，应强制退回编辑模式
            reply(startEvt, s"${JamConfig.name}睡得太香了没做梦……请稍后再试")
            None
        }
      }
    }.map {
      case Some(xid) => dreamingLoop(data, xid)
      case None => // 此处已经退回到编辑状态了，所以不需要做任何操作
    }
  }

  /**
   * 梦境循环，循环直到有梦境被记录
   *
   * @param data  会话数据
   * @param xid   梦境 ID
   * @param count 重试次数
   */
  private def dreamingLoop(data: Data, xid: String, count: Int = 1): Unit = {
    if (count > 30) {
      // 失败次数过多时退回编辑模式
      dreamFailedCallback(data)
    } else {
      Future {
        DreamClient.dreamLoop(data.uid, data.nid.get, xid) match {
          case Right(dreams) =>
            if (dreams.isEmpty) {
              // 梦境尚未记录完毕
              JamContext.actorSystem.scheduler.scheduleOnce(3.seconds) {
                Future {
                  // 递归调用，等待记录结果
                  dreamingLoop(data, xid, count + 1)
                }
              }
            } else dreamSuccessCallback(data, dreams)
          case Left(_) => dreamFailedCallback(data)
        }
      }
    }
  }

  /**
   * 入梦成功的回调函数
   *
   * @param data   会话数据
   * @param dreams 梦境记录
   */
  private def dreamSuccessCallback(data: Data, dreams: List[Dream]): Unit = {
    reply(startEvt, "已收集到如下梦境：")
    dreams.zipWithIndex.map {
      case (dream, idx) => s"梦境编号：${idx + 1}\n内容：${dream.content}"
    }.foreach(reply(startEvt, _))
    reply(startEvt,
      """发送 +梦境编号 来将指定的梦境变为现实
        |发送 -再入梦 可以刷新梦境
        |发送 -返回 取消并回到编辑模式
        |发送 -帮助 打开帮助菜单""".stripMargin)
    become(dreaming(data, dreams))
  }

  /**
   * 入梦失败的回调函数
   *
   * @param data 会话数据
   */
  private def dreamFailedCallback(data: Data): Unit = {
    reply(startEvt, s"${JamConfig.name}的大脑选择性的忘记了梦境……请稍后再试")
    become(writing(data))
    reply(startEvt, s"已返回编辑模式")
  }

  /**
   * 退出
   * 退出前将文章发送给原始会话和所有连接的聊天会话
   *
   * @param data 会话数据
   * @param evt  消息事件
   */
  private def exit(data: Data, evt: Event): Unit = {
    become(skipping(data))
    val finalContent = s"${if (data.title.isBlank) "无题" else s"《${data.title.trim}》"}\n${data.content}"
    reply(startEvt, s"最终的文稿：")
    reply(startEvt, finalContent)
    evt.sender ! ContentUpdated(self, finalContent, isAppend = true, exitEvent = true)
    reply(startEvt, s"${JamConfig.name}苏醒了，好像，头有点痛？")
    context.stop(self)
  }
}

object DreamActor {

  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(classOf[DreamActor])

  /**
   * 数据
   * - 梦境相关数据属于临时数据，不保存在状态中
   */
  private case class Data
  (
    uid: String,
    mid: String,
    models: List[AICharacter],
    signature: String,
    title: String = "",
    nid: Option[String] = None,
    lastContent: String = "",
    content: String = "",
    isExiting: Boolean = false
  )

  case class Event(sender: ActorRef, evt: EventMessage)

  /**
   * 内容更新事件
   * - 追加模式：delta = 追加
   * - 覆盖模式：delta = 全文
   * - 当会话结束时，向全部订阅者发送全文
   */
  case class ContentUpdated(sender: ActorRef, delta: String, isAppend: Boolean, exitEvent: Boolean = false)

  case class Start(sender: ActorRef)

  case class Ready(sender: ActorRef)

  case class FailToStart(sender: ActorRef)

}
