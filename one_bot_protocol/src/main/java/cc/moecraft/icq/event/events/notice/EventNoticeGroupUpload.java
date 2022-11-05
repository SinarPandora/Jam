package cc.moecraft.icq.event.events.notice;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.*;

/**
 * 群文件上传事件
 *
 * @author Hykilpikonna
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Setter(AccessLevel.NONE)
@ToString(callSuper = true)
public class EventNoticeGroupUpload extends EventNotice {
    @SerializedName("file")
    @Expose
    protected File file;

    @SerializedName("group_id")
    @Expose
    protected Long groupId;

    @Data
    @Setter(AccessLevel.NONE)
    public class File {
        @SerializedName("busid")
        @Expose
        protected Long busid;

        @SerializedName("id")
        @Expose
        protected String id;

        @SerializedName("name")
        @Expose
        protected String name;

        @SerializedName("size")
        @Expose
        protected Long size;
    }

    @Override
    public boolean contentEquals(Object o) {
        if (!(o instanceof EventNoticeGroupUpload)) return false;
        EventNoticeGroupUpload other = (EventNoticeGroupUpload) o;

        return super.contentEquals(o) &&
                other.getGroupId().equals(getGroupId()) &&
                other.getFile().equals(getFile());
    }
}
