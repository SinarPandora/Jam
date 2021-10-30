package o.lartifa.jam.cool.qq.command

import cc.moecraft.icq.command.interfaces.IcqCommand
import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.user.User
import o.lartifa.jam.common.config.BotConfig
import o.lartifa.jam.common.protocol.{Done, Exit, Fail}
import o.lartifa.jam.common.util.{ExtraActor, GlobalConstant}
import o.lartifa.jam.cool.qq.command.base.MasterEverywhereCommand
import o.lartifa.jam.cool.qq.listener.BanList
import o.lartifa.jam.engine.JamLoader
import o.lartifa.jam.model.behaviors.ActorCreator
import o.lartifa.jam.model.tasks.{GoASleep, WakeUp}
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.plugins.JamPluginLoader
import o.lartifa.jam.plugins.caiyunai.dream.DreamingActorProtocol.Login
import o.lartifa.jam.plugins.caiyunai.dream.KeepAliveDreamingActor
import o.lartifa.jam.pool.{JamContext, ThreadPools}

import java.util
import scala.async.Async.*
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

/**
 * 监护人指令
 *
 * Author: sinar
 * 2020/1/5 03:12
 */
object MasterCommands {

  private implicit val exec: ExecutionContext = ThreadPools.DEFAULT

  def commands: List[IcqCommand] = List(
    Ping, SessionInfo, Refresh, ReloadSSDL,
    ListVariable, ClearVariableInChat, SetVariable, RemoveVariable,
    ListPlugins, EnablePlugin, DisablePlugin, UninstallPlugin,
    WakeUpNow, GoASleepNow, ShowRawMessage, BanChat, AllowChat, ShowBanList,
    DreamClientLogin
  ) ++ JamPluginLoader.loadedComponents.masterCommands

  private object Ping extends MasterEverywhereCommand("ping", "在吗") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] =
      Future.successful("我在")
  }

  private object ListVariable extends MasterEverywhereCommand("列出变量") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = async {
      val pool = JamContext.variablePool
      val result: String = await(pool.listAll()).sortBy(_.chatId).map(row => s"ID：${row.chatId}，变量名：${row.name}，值：${row.value}").mkString("\n")
      if ("" == result) event.respond("当前会话没有变量哦")
      result
    }
  }

  private object ClearVariableInChat extends MasterEverywhereCommand("清空变量", "清理变量") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = async {
      val pool = JamContext.variablePool
      await(pool.cleanAllInChat(event))
      "我已经把它们忘掉啦！"
    }
  }

  private object SetVariable extends MasterEverywhereCommand("设置变量") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = async {
      if (args.size() != 2) {
        // 输入不合法
        "？"
      } else {
        val name = args.get(0)
        val value = args.get(1)
        val pool = JamContext.variablePool
        await(pool.updateOrElseSet(name, value)(CommandExecuteContext(event)).map(_ => "变量设置成功！"))
      }
    }
  }

  private object RemoveVariable extends MasterEverywhereCommand("删除变量") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = async {
      if (args.isEmpty) {
        "？？"
      } else {
        val name = args.get(0)
        val pool = JamContext.variablePool
        await(pool.delete(name)(CommandExecuteContext(event)).map(_ => "变量删除成功！"))
      }
    }
  }

  private object ReloadSSDL extends MasterEverywhereCommand("重新解析", "R") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = async {
      await(JamLoader.reloadSSDL()(CommandExecuteContext(event)))
      NO_RESPONSE
    }
  }

  private object SessionInfo extends MasterEverywhereCommand("聊天信息", "会话信息") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = async {
      val ChatInfo(chatType, chatId) = ChatInfo(event)
      s"""会话类型为：$chatType
         |会话 ID 为：$chatId""".stripMargin
    }
  }

  private object Refresh extends MasterEverywhereCommand("刷新", "刷新缓存") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = async {
      event.getBot.getAccountManager.refreshCache()
      "缓存刷新成功"
    }
  }

  private object ListPlugins extends MasterEverywhereCommand("列出已安装插件", "列出插件") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = async {
      val plugins = await(JamPluginLoader.listPlugin()).map(it => {
        s"${it.id}：[${if (it.isEnabled) "启用" else "禁用"}]${it.name}，" +
          s"包名：${it.`package`}，作者：${it.author}"
      })
      if (plugins.isEmpty) "当前没有安装任何插件"
      else "当前已安装的插件列表如下：\n" + plugins.mkString("\n")
    }
  }

  private object EnablePlugin extends MasterEverywhereCommand("启用插件") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = {
      if (args.isEmpty) {
        Future.successful("请指定要禁用的插件编号")
      } else {
        Future.sequence {
          argsToIds(event, args, "插件")
            .map(id => JamPluginLoader.enablePlugin(event, id))
        }.map(_ => NO_RESPONSE)
      }
    }
  }

  private object DisablePlugin extends MasterEverywhereCommand("禁用插件") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = {
      if (args.isEmpty) {
        Future.successful("请指定要启用用的插件编号")
      } else {
        Future.sequence {
          argsToIds(event, args, "插件")
            .map(id => JamPluginLoader.disablePlugin(event, id))
        }.map(_ => NO_RESPONSE)
      }
    }
  }

  private object UninstallPlugin extends MasterEverywhereCommand("卸载插件") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = {
      if (args.isEmpty) {
        Future.successful("请指定要启用用的插件编号")
      } else {
        Future.sequence {
          argsToIds(event, args, "插件")
            .map(id => JamPluginLoader.uninstallPlugin(event, id))
        }.map(_ => NO_RESPONSE)
      }
    }
  }

  private object WakeUpNow extends MasterEverywhereCommand("唤醒", "嗅盐治晕倒") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = {
      WakeUp.wakeUp()
      Future.successful(s"${BotConfig.name}已苏醒")
    }
  }

  private object GoASleepNow extends MasterEverywhereCommand("休眠", "昏睡红茶") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = {
      GoASleep.goASleep()
      Future.successful(s"${BotConfig.name}已休眠")
    }
  }

  private object ShowRawMessage extends MasterEverywhereCommand("原始信息", "raw") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = Future {
      event.respond(event.getRawMessage, true)
      NO_RESPONSE
    }
  }

  private object BanChat extends MasterEverywhereCommand("禁止", "ban") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = {
      if (args.isEmpty) {
        val chatInfo@ChatInfo(chatType, chatId) = ChatInfo(event)
        (if (chatType == GlobalConstant.MessageType.PRIVATE) {
          BanList.user.add(chatId)
          JamContext.variablePool.update("Private_Ban_List", BanList.user.mkString(","))
        } else {
          BanList.group.add(chatId)
          JamContext.variablePool.update("Group_Ban_List", BanList.group.mkString(","))
        }).map(_ => s"已屏蔽当前会话：$chatInfo")
      } else if (args.size() == 2) {
        Try(args.get(1).toLong) match {
          case Failure(_) =>
            Future.successful("请输入正确的QQ号")
          case Success(value) =>
            (if (args.get(0) == "群") {
              BanList.group.add(value)
              JamContext.variablePool.update("Group_Ban_List", BanList.group.mkString(","))
            } else {
              BanList.user.add(value)
              JamContext.variablePool.update("Private_Ban_List", BanList.user.mkString(","))
            }).map(_ => s"已屏蔽会话：$value")
        }
      } else {
        Future.successful(
          """请输入正确的指令格式，示例：
            |屏蔽当前会话：!ban
            |屏蔽指定群聊：!ban 群 12345678
            |屏蔽指定用户：!ban 用户 12345678
            |列出禁言列表：!ban_list""".stripMargin)
      }
    }
  }

  private object AllowChat extends MasterEverywhereCommand("解禁", "allow") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = {
      if (args.isEmpty) {
        val chatInfo@ChatInfo(chatType, chatId) = ChatInfo(event)
        (if (chatType == GlobalConstant.MessageType.PRIVATE) {
          BanList.user.remove(chatId)
          JamContext.variablePool.update("Private_Ban_List", BanList.user.mkString(","))
        } else {
          BanList.group.remove(chatId)
          JamContext.variablePool.update("Group_Ban_List", BanList.group.mkString(","))
        }).map(_ => s"已解禁当前会话：$chatInfo")
      } else if (args.size() == 2) {
        Try(args.get(1).toLong) match {
          case Failure(_) =>
            Future.successful("请输入正确的QQ号")
          case Success(value) =>
            (if (args.get(0) == "群") {
              BanList.group.remove(value)
              JamContext.variablePool.update("Group_Ban_List", BanList.group.mkString(","))
            } else {
              BanList.user.remove(value)
              JamContext.variablePool.update("Private_Ban_List", BanList.user.mkString(","))
            }).map(_ => s"已解禁会话：$value")
        }
      } else {
        Future.successful(
          """请输入正确的指令格式，示例：
            |解禁当前会话：!allow
            |解禁指定群聊：!allow 群 12345678
            |解禁指定用户：!allow 用户 12345678
            |列出禁言列表：!ban_list""".stripMargin)
      }
    }
  }

  private object ShowBanList extends MasterEverywhereCommand("ban_list", "banList", "禁言列表") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = Future {
      event.respond(
        s"""已屏蔽的群：
           |${BanList.group.sliding(3, 3).map(_.mkString(" ")).mkString("\n")}""".stripMargin)
      s"""已屏蔽的用户：
         |${BanList.user.sliding(3, 3).map(_.mkString(" ")).mkString("\n")}""".stripMargin
    }
  }

  private object DreamClientLogin extends MasterEverywhereCommand("dream_login", "小梦登录") {
    /**
     * 指令操作
     *
     * @param event   消息事件
     * @param sender  发送者
     * @param command 指令内容
     * @param args    参数
     * @return 输出内容
     */
    override def task(event: EventMessage, sender: User, command: String, args: util.ArrayList[String]): Future[String] = async {
      val promise = Promise[Boolean]()
      ActorCreator.actorOf(ExtraActor(
        ctx => KeepAliveDreamingActor.instance ! Exit(ctx.self),
        _ => {
          case Done => promise.success(true)
        }
      ))
      await(promise.future)
      ActorCreator.actorOf(ExtraActor(
        ctx => KeepAliveDreamingActor.instance ! Login(ctx.self, event),
        _ => {
          case Done => // 什么也不做
          case Fail(errMsg) => event.respond(errMsg)
          case _ => event.respond("进入登录流程失败")
        }
      ))
      NO_RESPONSE
    }
  }
}
