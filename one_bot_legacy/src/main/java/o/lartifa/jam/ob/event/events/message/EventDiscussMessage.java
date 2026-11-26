package o.lartifa.jam.ob.event.events.message;

import o.lartifa.jam.ob.sender.returndata.ReturnData;
import o.lartifa.jam.ob.sender.returndata.returnpojo.send.RMessageReturnData;
import o.lartifa.jam.ob.user.Group;
import o.lartifa.jam.ob.user.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 讨论组消息事件
 *
 * @author Hykilpikonna
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class EventDiscussMessage extends EventGroupOrDiscussMessage
{
    @SerializedName("discuss_id")
    @Expose
    protected Long discussId;

    @Override
    public ReturnData<RMessageReturnData> respond(String message, boolean raw)
    {
        return getHttpApi().sendDiscussMsg(discussId, message, raw);
    }

    @Override
    public Group getGroup()
    {
        return getBot().getGroupManager().getGroupFromID(discussId);
    }

    @Override
    public void kick()
    {
        getHttpApi().setGroupKick(discussId, senderId);
    }

    @Override
    public User getSender()
    {
        return getBot().getUserManager().getUserFromID(senderId);
    }

    @Override
    public boolean contentEquals(Object o)
    {
        if (!(o instanceof EventDiscussMessage)) return false;
        EventDiscussMessage other = (EventDiscussMessage) o;

        return super.contentEquals(o) && other.getDiscussId().equals(this.getDiscussId());
    }
}
