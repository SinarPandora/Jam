package cc.moecraft.icq.event.events.notice;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.*;

/**
 * 好友消息撤回事件
 *
 * @author CrazyKid (i@crazykid.moe)
 * @since 2020/12/30 21:16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Setter(AccessLevel.NONE)
@ToString(callSuper = true)
public class EventNoticeFriendRecall extends EventNotice {
    /**
     * 被撤回的消息 ID
     */
    @SerializedName("message_id")
    @Expose
    protected Long messageId;

    @Override
    public boolean contentEquals(Object o) {
        if (!(o instanceof EventNoticeFriendRecall)) return false;
        EventNoticeFriendRecall other = (EventNoticeFriendRecall) o;

        return super.contentEquals(o) &&
                other.getMessageId().equals(this.getMessageId());
    }
}
