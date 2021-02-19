package o.lartifa.jam.cool.qq.listener.fsm

import o.lartifa.jam.model.Executable
import o.lartifa.jam.model.behaviors.ReplyToFriend

/**
 * 消息处理模式
 */
trait Mode extends Executable[ModeRtnCode] with ReplyToFriend
