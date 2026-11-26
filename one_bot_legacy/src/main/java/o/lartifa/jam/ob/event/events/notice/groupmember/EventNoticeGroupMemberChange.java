package o.lartifa.jam.ob.event.events.notice.groupmember;

import o.lartifa.jam.ob.accounts.BotAccount;
import o.lartifa.jam.ob.event.events.notice.EventNotice;
import o.lartifa.jam.ob.event.methodsets.GroupEventMethods;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.*;

/**
 * 群员数量更改事件
 *
 * @author Hykilpikonna
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Setter(AccessLevel.NONE)
@ToString(callSuper = true)
public class EventNoticeGroupMemberChange extends EventNotice
{
    @SerializedName("group_id")
    @Expose
    protected Long groupId;

    @SerializedName("operator_id")
    @Expose
    protected Long operatorId;

    @SerializedName("sub_type")
    @Expose
    protected String subType;

    private GroupEventMethods groupMethods = null;

    @Override
    public boolean contentEquals(Object o)
    {
        if (!(o instanceof EventNoticeGroupMemberChange)) return false;
        EventNoticeGroupMemberChange other = (EventNoticeGroupMemberChange) o;

        return super.contentEquals(o) &&
                other.getGroupId().equals(getGroupId()) &&
                other.getOperatorId().equals(getOperatorId()) &&
                other.getSubType().equals(getSubType());
    }

    public GroupEventMethods getGroupMethods()
    {
        if (groupMethods != null)
        {
            return groupMethods;
        }
        return groupMethods = new GroupEventMethods(this, groupId);
    }

    @Override
    public BotAccount getBotAccount()
    {
        return getGroupMethods().getBotAccount();
    }
}
