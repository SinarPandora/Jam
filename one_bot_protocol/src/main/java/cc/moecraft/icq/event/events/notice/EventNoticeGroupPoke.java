package cc.moecraft.icq.event.events.notice;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.*;

/**
 * 群戳一戳事件
 *
 * @author CrazyKid (i@crazykid.moe)
 * @since 2020/12/30 21:36
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Setter(AccessLevel.NONE)
@ToString(callSuper = true)
public class EventNoticeGroupPoke extends EventNotice {
    /**
     * 被戳者 QQ 号
     */
    @SerializedName("target_id")
    @Expose
    protected Long targetId;

    /**
     * 群号
     */
    @SerializedName("group_id")
    @Expose
    protected Long groupId;

    /**
     * 事件子类型
     */
    @SerializedName("sub_type")
    @Expose
    protected String subType;

    @Override
    public boolean contentEquals(Object o) {
        if (!(o instanceof EventNoticeGroupPoke)) return false;
        EventNoticeGroupPoke other = (EventNoticeGroupPoke) o;

        return super.contentEquals(o) &&
                other.getTargetId().equals(this.getTargetId()) &&
                other.getGroupId().equals(this.getGroupId());
    }
}
