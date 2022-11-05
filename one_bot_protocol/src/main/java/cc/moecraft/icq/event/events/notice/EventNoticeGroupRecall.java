package cc.moecraft.icq.event.events.notice;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.*;

/**
 * 群消息撤回事件
 *
 * @author CrazyKid (i@crazykid.moe)
 * @since 2020/12/29 21:14
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Setter(AccessLevel.NONE)
@ToString(callSuper = true)
public class EventNoticeGroupRecall extends EventNotice {
    /**
     * 群号
     */
    @SerializedName("group_id")
    @Expose
    protected Long groupId;

    /**
     * 撤回操作者 QQ 号
     */
    @SerializedName("operator_id")
    @Expose
    protected Long operatorId;

    /**
     * 被撤回的消息 ID
     */
    @SerializedName("message_id")
    @Expose
    protected Long messageId;

    @Override
    public boolean contentEquals(Object o) {
        if (!(o instanceof EventNoticeGroupRecall)) return false;
        EventNoticeGroupRecall other = (EventNoticeGroupRecall) o;

        return super.contentEquals(o) &&
                other.getGroupId().equals(this.getGroupId()) &&
                other.getOperatorId().equals(this.getOperatorId()) &&
                other.getMessageId().equals(this.getMessageId());
    }
}
