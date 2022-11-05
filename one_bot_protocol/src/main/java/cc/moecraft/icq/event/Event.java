package cc.moecraft.icq.event;

import cc.moecraft.icq.PicqConstants;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 事件
 *
 * @author Hykilpikonna
 */
@Data
public abstract class Event implements ContentComparable {
    @SerializedName("post_type")
    @Expose
    protected String postType;

    @SerializedName("self_id")
    @Expose
    public Long selfId;

    @SerializedName("time")
    @Expose
    protected Long time;

    @Override
    public boolean contentEquals(Object o) {
        if (!(o instanceof Event)) return false;
        Event other = (Event) o;

        // ID 不能用来判断是不是相等...
        // 因为不同酷Q端发来的的ID不一样啦w
        return Math.abs(other.getTime() - this.getTime()) < PicqConstants.MAO_JUDGEMENT_TIME_INTERVAL_SEC;
    }
}
