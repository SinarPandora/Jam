package o.lartifa.jam.plugins.caiyunai.dream

import akka.actor.{Actor, ActorRef, Cancellable}
import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.logger.HyLogger
import cn.hutool.core.util.NumberUtil
import o.lartifa.jam.common.config.{BotConfig, PluginConfig}
import o.lartifa.jam.common.protocol.{Done, Exit, Fail, Data as Resp}
import o.lartifa.jam.common.util.{ExtraActor, MasterUtil}
import o.lartifa.jam.cool.qq.listener.interactive.Interactive
import o.lartifa.jam.model.SpecificSender
import o.lartifa.jam.model.behaviors.ActorCreator
import o.lartifa.jam.plugins.caiyunai.dream.DreamClient.AICharacter
import o.lartifa.jam.plugins.caiyunai.dream.DreamingActorProtocol.*
import o.lartifa.jam.pool.JamContext
import requests.Session

import java.util.concurrent.Executors
import scala.async.Async.{async, await}
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}

/**
 * 保持彩云小梦服务时刻处于可用状态
 *
 * Author: sinar
 * 2021/10/23 02:17
 */
object KeepAliveDreamingActor extends Actor {
  that =>
  case class Data(session: Session, uid: String, retry: Int = 0, keepAliveTask: Cancellable, models: List[AICharacter])
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(KeepAliveDreamingActor.getClass)

  private def config: PluginConfig.DreamAI = PluginConfig.config.dreamAI

  // 为避免多网络请求阻塞其他消息处理，创建单独线程池
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

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
      } else login(senderRef, eventMessage)
    case Reply(senderRef, _) =>
      senderRef ! Fail("尚未登录小梦")
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
    eventMessage.respond("%s，彩云小梦需要手动登录，验证码短信已发送")
    eventMessage.respond("请输入验证码")
    Interactive.interact(SpecificSender.privateOf(BotConfig.qID)) { (s, evt) =>
      val codeStr = evt.message.trim
      if (NumberUtil.isNumber(codeStr)) {
        val uid = DreamClient.phoneLogin(config.mobile, codeStr, codeId) match {
          case Left(errMsg) => senderRef ! Fail(errMsg); return
          case Right(value) => value
        }
        val models = DreamClient.listModels match {
          case Left(errMsg) => senderRef ! Fail(errMsg); return
          case Right(value) => value
        }
        val keepAliveTask: Cancellable = context.system.scheduler.scheduleAtFixedRate(1.minute, 1.minute, this.self, KeepAlive)
        // 切换为登录准备模式
        that.context.become({
          case Done =>
            handleStage(Data(session, uid, 0, keepAliveTask, models))
          case Exit =>
            context.become(initStage())
        })
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
    if (data.retry > 5) {
      logger.warning("彩云小梦登录已过期")
      MasterUtil.notifyMaster("%s，小梦的登录已过期，请重新登录")
      data.keepAliveTask.cancel()
      that.context.become(initStage())
    } else {
      that.context.become({
        case Reply(senderRef, content) => dreaming(data, content, senderRef)
        case KeepAlive =>
          ActorCreator.actorOf(new ExtraActor() {
            override def onStart(): Unit = that.self ! Reply(self, "彩云小梦")

            override def handle: Receive = {
              case Resp(_) => logger.debug("彩云小梦正常运行中")
              case Fail(msg) =>
                logger.warning(msg)
                handleStage(data.copy(retry = data.retry + 1))
            }
          })
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
          handleStage(data.copy(retry = data.retry + 1))
        case Right(meta) =>
          DreamClient.dreaming(data.uid, content, meta) match {
            case Left(errMsg) =>
              senderRef ! Fail(errMsg)
              handleStage(data.copy(retry = data.retry + 1))
            case Right(dreams) =>
              senderRef ! Resp(dreams)
              if (data.retry != 0) {
                handleStage(data.copy(retry = 0))
              }
          }
      }
    }
  }
}
