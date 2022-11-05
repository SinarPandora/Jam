package cc.moecraft.icq.event.events.message;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.*;

/**
 * 群消息事件
 *
 * @author Hykilpikonna
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class EventGroupMessage extends EventGroupOrDiscussMessage {
    @SerializedName("anonymous")
    @Expose
    protected Anonymous anonymous;

    @SerializedName("group_id")
    @Expose
    protected Long groupId;

    @SerializedName("sub_type")
    @Expose
    protected String subType;

    @Data
    @Setter(AccessLevel.NONE)
    public class Anonymous {
        @SerializedName("flag")
        @Expose
        protected String flag;

        @SerializedName("id")
        @Expose
        protected Long id;

        @SerializedName("name")
        @Expose
        protected String name;
    }

    /**
     * 获取发送者是不是匿名状态
     *
     * @return 是不是匿名
     */
    public boolean isSenderAnonymous() {
        return anonymous != null;
    }

    @Override
    public boolean contentEquals(Object o) {
        if (!(o instanceof EventGroupMessage)) return false;
        EventGroupMessage other = (EventGroupMessage) o;

        return super.contentEquals(o) && other.getGroupId().equals(this.getGroupId());
    }
}
