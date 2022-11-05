package cc.moecraft.icq.event.events.message;

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
public class EventDiscussMessage extends EventGroupOrDiscussMessage {
    @SerializedName("discuss_id")
    @Expose
    protected Long discussId;

    @Override
    public boolean contentEquals(Object o) {
        if (!(o instanceof EventDiscussMessage)) return false;
        EventDiscussMessage other = (EventDiscussMessage) o;

        return super.contentEquals(o) && other.getDiscussId().equals(this.getDiscussId());
    }
}
