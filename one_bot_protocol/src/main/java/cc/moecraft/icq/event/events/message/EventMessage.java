package cc.moecraft.icq.event.events.message;

import cc.moecraft.icq.event.Event;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static cc.moecraft.icq.utils.CQUtils.removeCqCode;

/**
 * 消息事件
 *
 * @author Hykilpikonna
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public abstract class EventMessage extends Event {
    @SerializedName("message_type")
    @Expose
    protected String messageType;

    @SerializedName("font")
    @Expose
    protected Long font;

    @SerializedName("message")
    @Expose
    public String message;

    @SerializedName("message_id")
    @Expose
    protected Long messageId;

    @SerializedName("raw_message")
    @Expose
    protected String rawMessage;

    @SerializedName("user_id")
    @Expose
    protected Long senderId;

    @Override
    public boolean contentEquals(Object o) {
        if (!(o instanceof EventMessage)) return false;
        EventMessage other = (EventMessage) o;

        return super.contentEquals(o) &&
                other.getSenderId().equals(this.getSenderId()) &&
                other.getMessageType().equals(this.getMessageType()) &&
                removeCqCode(other.getMessage()).equals(removeCqCode(this.getMessage()));
    }
}
