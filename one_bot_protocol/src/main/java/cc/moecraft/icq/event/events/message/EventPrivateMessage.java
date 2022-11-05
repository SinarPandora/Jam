package cc.moecraft.icq.event.events.message;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 私聊消息事件
 *
 * @author Hykilpikonna
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class EventPrivateMessage extends EventMessage {
    @SerializedName("sub_type")
    @Expose
    protected String subType;
}
