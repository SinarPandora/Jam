package o.lartifa.jam.pool

import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.common.exception.{ExecutionException, VarNotFoundException}
import o.lartifa.jam.common.util.EventMessageHelper._
import o.lartifa.jam.database.temporary.schema.Tables
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}

import java.sql.Timestamp
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/**
 * 临时变量（指令参数）工具
 *
 * Author: sinar
 * 2020/7/17 21:08
 */
class TempVarPool(eventMessage: EventMessage, commandStartTime: Timestamp)(implicit exec: ExecutionContext) extends VariablePool {

  private val PREDEF_VARIABLES: Set[String] = Set("Bot昵称", "自己的昵称", "BotQQ昵称", "自己的QQ昵称", "群号", "群名",
    "发送者昵称", "对方昵称", "发送者群昵称", "对方群昵称", "发送者QQ昵称", "对方QQ昵称", "发送者QQ", "对方QQ", "QQ号",
    "发送者年龄", "对方年龄", "发送者性别", "对方性别", "会话类型", "是否为好友", "对方管理状态")

  private val CommandScopeParameters: mutable.Map[String, String] = mutable.Map.empty

  /**
   * 更新变量，不存在时报错
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 更新结果
   */
  override def update(name: String, value: String)(implicit context: CommandExecuteContext): Future[String] = {
    if (checkOverridable(name)) {
      if (CommandScopeParameters.contains(name)) {
        CommandScopeParameters += name -> value
        Future.successful(value)
      } else Future.failed(VarNotFoundException(name, "临时变量"))
    } else Future.failed(ExecutionException(s"临时变量 $name 不可被覆盖！"))
  }

  /**
   * 更新变量，若变量不存在，则将其设置默认值
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 执行结果
   */
  override def updateOrElseSet(name: String, value: String)(implicit context: CommandExecuteContext): Future[String] = {
    if (checkOverridable(name)) {
      CommandScopeParameters += name -> value
      Future.successful(value)
    } else Future.failed(ExecutionException(s"临时变量 $name 不可被覆盖！"))
  }

  /**
   * 更新变量，若变量不存在，则使用默认值（不更新）
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 执行结果
   */
  override def updateOrElseUse(name: String, value: String)(implicit context: CommandExecuteContext): Future[String] = {
    if (checkOverridable(name)) {
      if (CommandScopeParameters.contains(name)) {
        CommandScopeParameters += name -> value
      }
      Future.successful(value)
    } else Future.failed(ExecutionException(s"临时变量 $name 不可被覆盖！"))
  }

  /**
   * 获取变量
   *
   * @param name    变量名
   * @param context 执行上下文
   * @return 变量值（Optional）
   */
  override def get(name: String)(implicit context: CommandExecuteContext): Future[Option[String]] = Future {
    getVar(name)
  }

  /**
   * 取或更新变量
   *
   * @param name    变量名
   * @param orElse  若不存在，则用该值更新并返回
   * @param context 执行上下文
   * @return 变量值
   */
  override def getOrElseUpdate(name: String, orElse: String)(implicit context: CommandExecuteContext): Future[String] = Future {
    getVar(name).getOrElse {
      CommandScopeParameters += name -> orElse
      orElse
    }
  }

  /**
   * 删除变量
   *
   * @param name    变量名
   * @param context 执行上下文
   * @return true：删除成功
   */
  override def delete(name: String)(implicit context: CommandExecuteContext): Future[Boolean] = {
    CommandScopeParameters.remove(name)
    Future.successful(true)
  }

  /**
   * 清除当前会话中的全部变量
   *
   * @param eventMessage 消息事件
   * @return true：删除成功
   */
  override def cleanAllInChat(eventMessage: EventMessage): Future[Boolean] = cleanAll()

  /**
   * 清除全部变量
   *
   * @return true：删除成功
   */
  override def cleanAll(): Future[Boolean] = {
    CommandScopeParameters.clear()
    Future.successful(true)
  }

  /**
   * 清除全部变量
   *
   * @return 全部变量
   */
  override def listAll(): Future[Seq[Tables.VariablesRow]] = Future {
    val chatInfo = ChatInfo(eventMessage)
    CommandScopeParameters.map {
      case (key, value) => Tables.VariablesRow(-1, key, chatInfo.chatType, chatInfo.chatId, value, "TEXT", commandStartTime)
    }.toSeq
  }

  /**
   * 清除全部变量
   *
   * @param eventMessage 消息事件
   * @return 全部变量
   */
  override def listAllInChart(eventMessage: EventMessage): Future[Seq[Tables.VariablesRow]] = listAll()

  /**
   * 获取临时变量
   *
   * @param name 参数名
   * @return 函数
   */
  private def getVar(name: String): Option[String] = Some {
    name match {
      case "Bot昵称" | "自己的昵称" => eventMessage.selfNickname
      case "BotQQ昵称" | "自己的QQ昵称" => eventMessage.selfQQName
      case "群号" => eventMessage.toGroupMessage.getGroupId.toString
      case "群名" => eventMessage.toGroupMessage.getGroup.refreshInfo().getGroupName
      case "发送者昵称" | "对方昵称" | "发送者群昵称" | "对方群昵称" => eventMessage.senderNickName
      case "发送者QQ昵称" | "对方QQ昵称" => eventMessage.senderQQName
      case "发送者QQ" | "对方QQ" | "QQ号" => eventMessage.getSenderId.toString
      case "发送者年龄" | "对方年龄" => eventMessage.getSender.refreshInfo(true).getAge.toString
      case "发送者性别" | "对方性别" => eventMessage.getSender.refreshInfo(true).getSex
      case "会话类型" => if (eventMessage.chatInfo.chatType == "private") "私聊" else "群聊"
      case "是否为好友" => if (eventMessage.toGroupMessage.getGroupSender.getInfo.getUnfriendly) "否" else "是"
      case "对方管理状态" => if (eventMessage.isSenderManager) "是" else "否"
      case other => return CommandScopeParameters.get(other)
    }
  }

  /**
   * 检查是否可以被覆盖
   *
   * @param name 变量名
   * @return 是否可以覆盖
   */
  private def checkOverridable(name: String): Boolean = !PREDEF_VARIABLES.contains(name)
}
