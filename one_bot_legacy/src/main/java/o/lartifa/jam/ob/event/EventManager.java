package o.lartifa.jam.ob.event;

import o.lartifa.jam.ob.PicqBotX;
import o.lartifa.jam.ob.command.CommandListener;
import o.lartifa.jam.ob.event.events.local.*;
import o.lartifa.jam.ob.event.events.message.*;
import o.lartifa.jam.ob.event.events.meta.EventMeta;
import o.lartifa.jam.ob.event.events.meta.EventMetaHeartbeat;
import o.lartifa.jam.ob.event.events.meta.EventMetaLifecycle;
import o.lartifa.jam.ob.event.events.notice.*;
import o.lartifa.jam.ob.event.events.notice.groupadmin.EventNoticeGroupAdminChange;
import o.lartifa.jam.ob.event.events.notice.groupadmin.EventNoticeGroupAdminRemove;
import o.lartifa.jam.ob.event.events.notice.groupadmin.EventNoticeGroupAdminSet;
import o.lartifa.jam.ob.event.events.notice.groupmember.EventNoticeGroupMemberChange;
import o.lartifa.jam.ob.event.events.notice.groupmember.decrease.EventNoticeGroupMemberDecrease;
import o.lartifa.jam.ob.event.events.notice.groupmember.decrease.EventNoticeGroupMemberKick;
import o.lartifa.jam.ob.event.events.notice.groupmember.decrease.EventNoticeGroupMemberKickBot;
import o.lartifa.jam.ob.event.events.notice.groupmember.decrease.EventNoticeGroupMemberLeave;
import o.lartifa.jam.ob.event.events.notice.groupmember.increase.EventNoticeGroupMemberApprove;
import o.lartifa.jam.ob.event.events.notice.groupmember.increase.EventNoticeGroupMemberIncrease;
import o.lartifa.jam.ob.event.events.notice.groupmember.increase.EventNoticeGroupMemberInvite;
import o.lartifa.jam.ob.event.events.request.EventFriendRequest;
import o.lartifa.jam.ob.event.events.request.EventGroupAddRequest;
import o.lartifa.jam.ob.event.events.request.EventGroupInviteRequest;
import o.lartifa.jam.ob.event.events.request.EventRequest;
import o.lartifa.jam.ob.utils.CQUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * 此类由 Hykilpikonna 在 2018/05/24 创建!
 * Created by Hykilpikonna on 2018/05/24!
 * Github: https://github.com/hykilpikonna
 * QQ: admin@moecraft.cc -OR- 871674895
 *
 * @author Hykilpikonna
 */
@Getter
public class EventManager
{
    private final PicqBotX bot;

    private final EventParser eventParser;

    @Setter
    private CommandListener commandListener;

    private ArrayList<IcqListener> registeredListeners = new ArrayList<>();

    private HashMap<String, ArrayList<RegisteredListenerMethod>> registeredMethods = new HashMap<>();

    /**
     * 所有可以注册的事件的类
     * 版本 v3.0.0.611 后, 反射注册类改为手动注册类
     * 因为这样可以减少 ≈600ms 的启动延迟
     */
    private static final List<Class<? extends Event>> eventClasses = asList(
            EventLocal.class,
            EventLocalException.class,
            EventLocalHttpFail.class,
            EventLocalHttpReceive.class,
            EventLocalSendDiscussMessage.class,
            EventLocalSendGroupMessage.class,
            EventLocalSendPrivateMessage.class,
            EventLocalSendMessage.class,

            EventDiscussMessage.class,
            EventGroupMessage.class,
            EventGroupOrDiscussMessage.class,
            EventPrivateMessage.class,
            EventMessage.class,

            EventNoticeGroupBan.class,
            EventNoticeGroupAdminChange.class,
            EventNoticeGroupAdminRemove.class,
            EventNoticeGroupAdminSet.class,
            EventNoticeGroupMemberDecrease.class,
            EventNoticeGroupMemberLeave.class,
            EventNoticeGroupMemberKick.class,
            EventNoticeGroupMemberKickBot.class,
            EventNoticeGroupMemberApprove.class,
            EventNoticeGroupMemberInvite.class,
            EventNoticeGroupMemberIncrease.class,
            EventNoticeGroupMemberChange.class,
            EventNoticeFriendAdd.class,
            EventNoticeGroupUpload.class,
            EventNoticeGroupRecall.class,
            EventNoticeFriendRecall.class,
            EventNoticeGroupPoke.class,
            EventNoticeFriendPoke.class,
            EventNoticeGroupLuckyKing.class,
            EventNoticeGroupHonor.class,
            EventNoticeGroupCard.class,
            EventNotice.class,

            EventFriendRequest.class,
            EventGroupAddRequest.class,
            EventGroupInviteRequest.class,
            EventRequest.class,

            EventMeta.class,
            EventMetaLifecycle.class,
            EventMetaHeartbeat.class
    );

    public EventManager(PicqBotX bot)
    {
        this.bot = bot;
        this.eventParser = new EventParser(this);
    }

    /**
     * 注册一个事件监听器
     *
     * @param listener 监听器
     */
    public void registerListener(IcqListener listener)
    {
        registeredListeners.add(listener);

        for (Method method : listener.getClass().getMethods())
        {
            if (method.getParameterCount() != 1)
            {
                continue;
            }

            Class<?> event = method.getParameterTypes()[0];

            if (!Event.class.isAssignableFrom(event))
            {
                continue;
            }
            if (!method.isAnnotationPresent(EventHandler.class))
            {
                continue;
            }

            for (Class<? extends Event> eventClass : eventClasses) // 向下注册所有子类
            {
                if (!event.isAssignableFrom(eventClass))
                {
                    continue;
                }

                String mapKey = eventClass.getName();

                if (registeredMethods.containsKey(mapKey))
                {
                    registeredMethods.get(mapKey).add(new RegisteredListenerMethod(method, listener));
                }
                else
                {
                    registeredMethods.put(mapKey, new ArrayList<>(Collections.singletonList(new RegisteredListenerMethod(method, listener))));
                }
            }
        }
    }

    /**
     * 注册很多个事件监听器
     *
     * @param listeners 很多个监听器
     */
    public void registerListeners(IcqListener... listeners)
    {
        for (IcqListener listener : listeners)
        {
            registerListener(listener);
        }
    }

    /**
     * 执行事件
     *
     * @param event 事件对象
     */
    public void call(Event event)
    {
        event.setBot(bot);

        if (event instanceof EventMessage)
        {
            ((EventMessage) event).message = CQUtils.decodeMessage(((EventMessage) event).getMessage());

            // 检查指令
            if (commandListener != null)
            {
                if (!commandListener.check((EventMessage) event))
                {
                    return;
                }
            }
        }

        String mapKey = event.getClass().getName();

        if (!registeredMethods.containsKey(mapKey))
        {
            return;
        }

        for (RegisteredListenerMethod registeredListenerMethod : registeredMethods.get(mapKey))
        {
            try
            {
                registeredListenerMethod.call(event);
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace(); // 这些理论上绝对不会出现
            }
            catch (Throwable e)
            {
                callError(event, e);
            }
        }
    }

    public void callError(Event event, Throwable throwable)
    {
        if (event instanceof EventLocalException && ((EventLocalException) event).getParentEvent() instanceof EventLocalException)
        {
            throwable.printStackTrace(); // 如果这个事件是报错事件, 而且这个事件报的错也是报错事件的话, 怎么办呢....
        }
        else
        {
            call(new EventLocalException(throwable instanceof InvocationTargetException ? throwable.getCause() : throwable, event));
        }
    }
}
