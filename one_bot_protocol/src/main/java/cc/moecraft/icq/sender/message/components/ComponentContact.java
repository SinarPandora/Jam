package cc.moecraft.icq.sender.message.components;

import cc.moecraft.icq.sender.message.MessageComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 群分享组件
 * <p>
 * Author: sinar
 * 2020/12/27 00:18
 */
@Getter
@AllArgsConstructor
public class ComponentContact extends MessageComponent {
    private final long qId;

    private final ContactType type;

    @Override
    public String toString() {
        return "[CQ:contact,type=" + type.toString() + ",id=" + qId + "]";
    }

    @AllArgsConstructor
    public enum ContactType {
        qq("qq"), group("group");

        public String type;

        @Override
        public String toString() {
            return type;
        }
    }
}
