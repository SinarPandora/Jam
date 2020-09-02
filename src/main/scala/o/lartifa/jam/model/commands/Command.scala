package o.lartifa.jam.model.commands

import o.lartifa.jam.model.Executable
import o.lartifa.jam.model.behaviors.ReplyToFriend

import scala.languageFeature.implicitConversions

/**
 * 指令原型
 *
 * Author: sinar
 * 2020/1/3 23:15
 */
abstract class Command[T] extends Executable[T] with ReplyToFriend
