package o.lartifa.jam.cool.qq.listener.fsm

/**
 * Router 返回值
 */
sealed trait ModeRtnCode

/**
 * 继续保持当前模式
 */
case object Continue extends ModeRtnCode

/**
 * 变更回普通模式
 */
case object UnBecome extends ModeRtnCode

/**
 * 保持当前模式并继续消息的捕获与执行
 */
case object ContinueThenParseMessage extends ModeRtnCode
