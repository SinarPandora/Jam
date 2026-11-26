package o.lartifa.jam.ob.event.methodsets;

import o.lartifa.jam.ob.accounts.BotAccount;
import o.lartifa.jam.ob.event.Event;
import o.lartifa.jam.ob.sender.returndata.ReturnData;
import o.lartifa.jam.ob.sender.returndata.returnpojo.send.RMessageReturnData;
import o.lartifa.jam.ob.user.Group;
import lombok.AllArgsConstructor;

/**
 * 此类由 Hykilpikonna 在 2018/08/26 创建!
 * Created by Hykilpikonna on 2018/08/26!
 * Github: https://github.com/hykilpikonna
 * QQ: admin@moecraft.cc -OR- 871674895
 *
 * @author Hykilpikonna
 */
@AllArgsConstructor
public class GroupEventMethods
{
    private Event event;

    private long groupId;

    /**
     * 回复消息
     *
     * @param message 消息
     * @return 消息发送结果
     */
    public ReturnData<RMessageReturnData> respond(String message)
    {
        return respond(message, false);
    }

    public ReturnData<RMessageReturnData> respond(String message, boolean raw)
    {
        return event.getHttpApi().sendGroupMsg(groupId, message, raw);
    }

    public BotAccount getBotAccount()
    {
        return event.getBot().getAccountManager().getOptimal(groupId);
    }

    public Group getGroup()
    {
        return event.getBot().getGroupManager().getGroupFromID(groupId);
    }
}
