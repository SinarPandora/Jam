package o.lartifa.jam.plugins

import akka.actor.ActorRef
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.model.behaviors.ActorCreator

import java.io.File
import scala.collection.mutable

/**
 * TRPG 包对象
 *
 * Author: sinar
 * 2021/7/17 20:13
 */
package object trpg {
  val ruleConfigFile: File = new File("plugin.trpg.rule_config_file")
  private[trpg] val gameManager: ActorRef = ActorCreator.actorOf(new TRPGGameManager())
  // 用了一个简单的方式防止管理器 Actor 重启后丢失数据
  private[trpg] val trpgGameRegistry: mutable.Map[ChatInfo, ActorRef] = mutable.Map.empty
}
