package o.lartifa.jam.model.commands

import o.lartifa.jam.model.CommandExecuteContext
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AsyncFlatSpec

/**
 * Author: sinar
 * 2020/1/23 17:29 
 */
class DoNotingTest extends AsyncFlatSpec with MockFactory {

  implicit private val commandContext: CommandExecuteContext = mock[CommandExecuteContext]

  it should "DoNoting 指令应该什么也不做，直接返回 true" in {
    DoNoting.execute() map (result => assert(result))
  }
}
