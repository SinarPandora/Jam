package cc.moecraft.icq.sender.message.components;

import cc.moecraft.icq.sender.message.MessageComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短视频组件
 * <p>
 * Author: sinar
 * 2020/12/27 02:44
 */
@Getter
@AllArgsConstructor
public class ComponentVideo extends MessageComponent {

    private final String file;

    @Override
    public String toString() {
        return "[CQ:video,file=" + file + "]";
    }
}
