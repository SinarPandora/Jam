package wrapper

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.sender.returndata.ReturnData
import cc.moecraft.icq.sender.returndata.returnpojo.send.RMessageReturnData
import o.lartifa.jam.common.util.AsyncUtil
import o.lartifa.jam.cool.qq.listener.interactive.Interactive
import o.lartifa.jam.cool.qq.listener.interactive.InteractiveFunction
import o.lartifa.jam.cool.qq.listener.interactive.InteractiveSession
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.SpecificSender
import scala.jdk.FutureConverters

import java.util.concurrent.CompletionStage
import java.util.function.BiConsumer

/**
 * Lambda DSL Wrapper
 *
 * Author: sinar
 * 2021/11/14 18:18
 */
class LambdaDSLWrapper {
    /**
     * 创建异步任务
     *
     * @param task 任务
     * @param seconds 延迟秒数
     * @return 任务 UUID
     */
    static String setTimeout(Runnable task, Double seconds = 0) {
        AsyncUtil.setTimeout(task, seconds)
    }

    /**
     * 清理异步任务
     *
     * @param uuid 任务 UUID
     * @return 清理成功时返回 true，若正在运行或已运行返回 false
     */
    static Boolean clearTimeout(String uuid) {
        AsyncUtil.clearTimeout(uuid)
    }

    static class Wrapper implements Interactive {
        private final CommandExecuteContext context

        Wrapper(CommandExecuteContext context) {
            this.context = context
        }

        /**
         * 回复消息
         *
         * @param message 消息内容
         * @return 回复结果
         */
        ReturnData<RMessageReturnData> reply(Object message) {
            context.eventMessage().respond(message.toString())
        }

        /**
         * 询问发送者
         *
         * @param behavior 行为
         * @param sender 消息发送者
         * @return 询问会话引用
         */
        CompletionStage<Object> ask(BiConsumer<InteractiveSession, EventMessage> behavior, SpecificSender sender = context.msgSender()) {
            FutureConverters.FutureOps(interact(sender, new InteractiveFunction() {
                @Override
                void apply(InteractiveSession session, EventMessage event) {
                    behavior.accept(session, event)
                }
            })).asJava()
        }
    }

    /**
     * 装配 DSL
     *
     * @param binding 脚本绑定
     * @param ctx 指令上下文
     */
    static void setUp(Binding binding, CommandExecuteContext ctx) {
        Wrapper wrapper = new Wrapper(ctx)
        binding.setVariable("setTimeout", LambdaDSLWrapper::setTimeout)
        binding.setVariable("clearTimeout", LambdaDSLWrapper::clearTimeout)
        binding.setVariable("reply", wrapper::reply)
        binding.setVariable("ask", wrapper::ask)
    }
}
