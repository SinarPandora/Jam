package o.lartifa.jam.cool.qq.listener

import o.lartifa.jam.model.ChatInfo

import java.util.concurrent.ConcurrentHashMap

package object fsm {
  private[fsm] val modes: ConcurrentHashMap[ChatInfo, Mode] = new ConcurrentHashMap()
}
