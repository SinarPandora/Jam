package o.lartifa.jam.plugins.caiyunai.dream

import org.apache.pekko.actor.{Actor, ActorRef, Cancellable, Props}
import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.logger.HyLogger
import cn.hutool.core.util.NumberUtil
import o.lartifa.jam.common.config.PluginConfig
import o.lartifa.jam.common.protocol.CommonProtocol.{Done, Exit, Fail, IsAlive_?, Offline, Online, Data as Resp}
import o.lartifa.jam.common.util.{ExtraActor, MasterUtil}
import o.lartifa.jam.cool.qq.listener.interactive.Interactive
import o.lartifa.jam.model.SpecificSender
import o.lartifa.jam.model.behaviors.ActorCreator
import o.lartifa.jam.plugins.caiyunai.dream.DreamClient.AICharacter
import o.lartifa.jam.plugins.caiyunai.dream.DreamingActorProtocol.*
import o.lartifa.jam.plugins.caiyunai.dream.KeepAliveDreamingActor.*
import o.lartifa.jam.pool.JamContext
import requests.Session

import java.util.concurrent.Executors
import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * 保持彩云小梦服务时刻处于可用状态
 *
 * Author: sinar
 * 2021/10/23 02:17
 */
class KeepAliveDreamingActor extends Actor {
  that =>
  case class Data(session: Session, uid: String, failCount: Int = 0, keepAliveTask: Cancellable, models: List[AICharacter])

  private def config: PluginConfig.DreamAI = PluginConfig.config.dreamAI

  override def receive: Receive = initStage()

  /**
   * 初始化阶段
   *
   * @return 行为
   */
  def initStage(): Receive = {
    case Login(senderRef, eventMessage) =>
      if (config.mobile.isBlank) {
        senderRef ! Fail("请设置手机号后再尝试登录彩云小梦")
      } else {
        login(senderRef, eventMessage)
        senderRef ! Done
      }
    case Reply(senderRef, _) => senderRef ! Fail("尚未登录小梦")
    case IsAlive_?(senderRef) => senderRef ! Offline
    case Exit(senderRef) => senderRef ! Done
  }

  /**
   * 登录
   *
   * @param senderRef    消息发送者
   * @param eventMessage 消息事件
   */
  def login(senderRef: ActorRef, eventMessage: EventMessage): Unit = {
    implicit val session: Session = requests.Session()
    val codeId = DreamClient.sendCaptcha(config.mobile) match {
      case Left(errMsg) => senderRef ! Fail(errMsg); return
      case Right(value) => value
    }
    eventMessage.respond(
      """彩云小梦需要手动登录，验证码短信已发送
        |（发送：退出 或 exit 离开登录流程）""".stripMargin)
    eventMessage.respond("请输入验证码")
    Interactive.interact(SpecificSender(eventMessage)) { (s, evt) =>
      if (evt.message.trim == "退出" || evt.message.trim.toLowerCase() == "exit") {
        evt.respond("已退出")
        s.release()
        s.break()
      }
      val codeStr = evt.message.trim
      if (NumberUtil.isNumber(codeStr)) {
        val uid = DreamClient.phoneLogin(config.mobile, codeStr, codeId) match {
          case Left(errMsg) => evt.respond(errMsg); s.break()
          case Right(value) => value
        }
        val models = DreamClient.listModels match {
          case Left(errMsg) => evt.respond(errMsg); s.break()
          case Right(value) => value
        }
        // TODO 暂时关闭健康检查，用以观察小梦多长时间会掉线
        // val keepAliveTask: Cancellable = context.system.scheduler
        //    .scheduleAtFixedRate(1.minute, 3.minutes, that.self, KeepAlive)
        // 切换为登录准备模式
        handleStage(Data(session, uid, 0, null, models))
        evt.respond("登录成功！")
        s.release()
      } else {
        evt.respond("请正确输入验证码")
      }
    }
  }

  /**
   * 处理阶段
   *
   * @param data 数据
   * @return 行为
   */
  def handleStage(data: Data): Unit = {
    if (data.failCount > 5) {
      logger.warning("彩云小梦登录已过期")
      MasterUtil.notifyMaster("%s，小梦的登录已过期，请重新登录")
      // data.keepAliveTask.cancel()
      that.context.become(initStage())
    } else {
      that.context.become({
        case IsAlive_?(senderRef) => senderRef ! Online
        case Exit(senderRef) =>
          that.context.become(initStage())
          senderRef ! Done
        case Reply(senderRef, content) => dreaming(data, content, senderRef)
        case KeepAlive =>
          ActorCreator.actorOf(ExtraActor(
            ctx => that.self ! Reply(ctx.self, "彩云小梦"),
            _ => {
              case Resp(_) => logger.debug("彩云小梦正常运行中")
              case Fail(msg) =>
                logger.warning(msg)
                handleStage(data.copy(failCount = data.failCount + 1))
            }
          ))
      })
    }
  }

  /**
   * 😴
   *
   * @param data      数据
   * @param content   原文
   * @param senderRef 发送者引用
   * @return 多个梦境
   */
  def dreaming(data: Data, content: String, senderRef: ActorRef): Future[Unit] = async {
    val value = await(JamContext.variablePool.getOrElseUpdate("彩云小梦默认AI编号", "0"))
    val modelId = value.toInt
    if (data.models.lengthIs <= modelId) {
      senderRef ! Fail("😴试图进入一个不存在的梦境……")
    } else {
      implicit val session: Session = data.session
      DreamClient.saveAtFirst(data.uid, content) match {
        case Left(errMsg) =>
          senderRef ! Fail(errMsg)
          handleStage(data.copy(failCount = data.failCount + 1))
        case Right(meta) =>
          DreamClient.dreaming(data.uid, data.models(modelId).mid, content, meta) match {
            case Left(errMsg) =>
              senderRef ! Fail(errMsg)
              handleStage(data.copy(failCount = data.failCount + 1))
            case Right(dreams) =>
              if (dreams.isEmpty) {
                handleStage(data.copy(failCount = data.failCount + 1))
              } else {
                senderRef ! Resp(dreams)
                if (data.failCount != 0) {
                  handleStage(data.copy(failCount = 0))
                }
              }
          }
      }
    }
  }
}

object KeepAliveDreamingActor {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(KeepAliveDreamingActor.getClass)
  val instance: ActorRef = ActorCreator.actorOf(Props(new KeepAliveDreamingActor()))
  // 为避免多网络请求阻塞其他消息处理，创建单独线程池
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

  /**
   * 输出启动信息
   */
  def showBootMessage(): Unit = {
    MasterUtil.notifyMaster("%s，联想回复需要登录才能使用，请发送：！小梦登录 进行登录")
  }
}
