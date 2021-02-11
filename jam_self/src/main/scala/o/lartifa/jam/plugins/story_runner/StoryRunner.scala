package o.lartifa.jam.plugins.story_runner

import akka.actor.{Actor, ActorRef}
import cc.moecraft.icq.event.events.message.EventMessage
import com.bladecoder.ink.runtime.Story
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.behaviors.ReplyToFriend
import o.lartifa.jam.plugins.story_runner.StoryRunner.Message._

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

/**
 * 故事运行器
 *
 * Author: sinar
 * 2021/2/2 20:47
 */
class StoryRunner(story: Story, save: List[Int], initEvent: EventMessage, config: StoryConfig) extends Actor with ReplyToFriend {

  val choice: ListBuffer[Int] = ListBuffer.from(save)

  override def preStart(): Unit = {
    reloadStory()
  }

  /**
   * TODO
   *   找一个 ink 中不存在的语法，然后把 Arc 脚本塞进去
   *   记录用户的选择
   * TODO
   *   故事进行有两种模式
   *   -> silence 模式：不执行输出和 Arc 脚本，用于读档
   *   -> normal 模式：执行输出和脚本
   *   当销毁存档并重新开始时，才销毁变量
   * TODO
   *   资源缓存
   * TODO
   *   StoryScopeVar
   *   魔法注释
   *   // @Magic:
   * TODO
   *  存档能否支持章节
   */
  override def receive: Receive = talking()

  /**
   * 讲述人模式
   *
   * @return 行为
   */
  def talking(): Receive = {
    case Continue(sender, ctx) =>
      while (story.canContinue) {
        // TODO 支持 Arc
        //      awaitable
        reply(story.Continue())(ctx)
      }
      if (!story.getCurrentChoices.isEmpty) {
        context.become(asking())
        showChoices(ctx)
      } else {
        sender ! TheEnd
      }
  }

  /**
   * 提问者模式
   *
   * @return 行为
   */
  def asking(): Receive = {
    case Choose(choice, sender, ctx) =>
      if (choice > 0 && choice < story.getCurrentChoices.size() + 1) {
        story.chooseChoiceIndex(choice - 1)
        saveChoice(choice - 1)
        context.become(talking())
        self ! Continue(sender, ctx)
      } else {
        reply("请做出正确的选择")(ctx)
        showChoices(ctx)
      }
  }

  /**
   * 展示可选项
   *
   * @param ctx 指令执行上下文
   */
  private def showChoices(ctx: CommandExecuteContext): Unit = {
    reply {
      story.getCurrentChoices.asScala.zipWithIndex.map {
        case (choice, idx) => s"${idx + 1}：$choice"
      }.mkString("\n")
    }(ctx)
  }

  /**
   * 保存选项
   *
   * @param option 当前选项
   */
  private def saveChoice(option: Int): Unit = {
    choice += option
    // TODO

  }

  /**
   * 重新加载故事
   */
  private def reloadStory(): Unit = {
    for (choice <- save) {
      story.continueMaximally()
      story.chooseChoiceIndex(choice)
    }
  }
}

object StoryRunner {

  object Message {

    /**
     * 让故事继续
     *
     * @param sender 发送者
     * @param ctx    指令执行上下文
     */
    case class Continue(sender: ActorRef, ctx: CommandExecuteContext)

    /**
     * 选择选项
     *
     * @param choice 选项
     * @param sender 发送者
     * @param ctx    指令执行上下文
     */
    case class Choose(choice: Int, sender: ActorRef, ctx: CommandExecuteContext)

    /**
     * 故事的终章
     */
    case object TheEnd

  }

}
