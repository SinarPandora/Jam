package o.lartifa.jam.pool

import java.sql.Timestamp

import ammonite.ops.PipeableImplicit
import cc.moecraft.icq.event.events.message.{EventGroupMessage, EventMessage}
import o.lartifa.jam.common.exception.{ExecutionException, VarNotFoundException}
import o.lartifa.jam.database.temporary.schema.Tables
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * 临时变量（指令参数）工具
 *
 * Author: sinar
 * 2020/7/17 21:08
 */
class TempVarPool(eventMessage: EventMessage, commandStartTime: Timestamp)(implicit exec: ExecutionContext) extends VariablePool {

  private val PREDEF_VARIABLES: Set[String] = Set("昵称", "群号", "群名", "群昵称", "发送者昵称", "对方昵称", "发送者QQ", "对方QQ",
    "发送者年龄", "对方性别", "发送者年龄", "对方性别", "会话类型", "发送者群昵称", "对方群昵称", "是否为好友")

  private val CommandScopeParameters: mutable.Map[String, String] = mutable.Map.empty

  /**
   * 更新变量，不存在时报错
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 更新结果
   */
  override def update(name: String, value: String)(implicit context: CommandExecuteContext): Future[String] = Future {
    if (checkOverridable(name)) {
      if (CommandScopeParameters.contains(name)) {
        CommandScopeParameters += name -> value
        value
      } else throw VarNotFoundException(name, "临时变量")
    } else throw ExecutionException(s"临时变量 $name 不可被覆盖！")
  }

  /**
   * 更新变量，若变量不存在，则将其设置默认值
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 执行结果
   */
  override def updateOrElseSet(name: String, value: String)(implicit context: CommandExecuteContext): Future[String] = Future {
    if (checkOverridable(name)) {
      CommandScopeParameters += name -> value
      value
    } else throw ExecutionException(s"临时变量 $name 不可被覆盖！")
  }

  /**
   * 更新变量，若变量不存在，则使用默认值（不更新）
   *
   * @param name    变量名
   * @param value   变量值
   * @param context 执行上下文
   * @return 执行结果
   */
  override def updateOrElseUse(name: String, value: String)(implicit context: CommandExecuteContext): Future[String] = Future {
    if (checkOverridable(name)) {
      if (CommandScopeParameters.contains(name)) {
        CommandScopeParameters += name -> value
      }
      value
    } else throw ExecutionException(s"临时变量 $name 不可被覆盖！")
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
  override def delete(name: String)(implicit context: CommandExecuteContext): Future[Boolean] = Future {
    CommandScopeParameters.remove(name)
    true
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
  override def cleanAll(): Future[Boolean] = Future {
    CommandScopeParameters.clear()
    true
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
  private def getVar(name: String): Option[String] = name match {
    case "昵称" => eventMessage.getBotAccount.getName |> Some.apply
    case "群号" => toGroupMessage(eventMessage).getGroupId |> Some.apply
    case "群名" => toGroupMessage(eventMessage).getGroup.getInfo.getGroupName |> Some.apply
    case "群昵称" => toGroupMessage(eventMessage).getGroupUser(eventMessage.getSenderId).getInfo.getNickname |> Some.apply
    case "发送者昵称" | "对方昵称" => eventMessage.getSender.getInfo.getNickname |> Some.apply
    case "发送者QQ" | "对方QQ" => eventMessage.getSender.getInfo.getUserId |> Some.apply
    case "发送者年龄" | "对方性别" => eventMessage.getSender.getInfo.getAge |> Some.apply
    case "发送者年龄" | "对方性别" => eventMessage.getSender.getInfo.getSex |> Some.apply
    case "会话类型" => ChatInfo(eventMessage).chatType |> Some.apply
    case "发送者群昵称" | "对方群昵称" => toGroupMessage(eventMessage).getGroupSender.getInfo.getNickname |> Some.apply
    case "是否为好友" => (if (toGroupMessage(eventMessage).getGroupSender.getInfo.getUnfriendly) "是" else "否") |> Some.apply
    case other => CommandScopeParameters.get(other)
  }

  /**
   * 检查是否可以被覆盖
   *
   * @param name 变量名
   * @return 是否可以覆盖
   */
  private def checkOverridable(name: String): Boolean = PREDEF_VARIABLES.contains(name)

  /**
   * 尝试将消息转换成群消息
   *
   * @param eventMessage 消息事件
   * @return 群消息
   */
  @throws[ExecutionException]
  private def toGroupMessage(eventMessage: EventMessage): EventGroupMessage =
    Try(eventMessage.asInstanceOf[EventGroupMessage])
      .getOrElse(throw ExecutionException("不能从非群聊获取群昵称"))
}
