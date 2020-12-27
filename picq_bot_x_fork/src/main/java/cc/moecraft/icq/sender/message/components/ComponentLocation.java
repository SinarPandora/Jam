package cc.moecraft.icq.sender.message.components;

import cc.moecraft.icq.sender.message.MessageComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 位置分享组件
 * <p>
 * lat：纬度
 * lon： 经度
 * <p>
 * Author: sinar
 * 2020/12/27 02:38
 */
@Getter
@AllArgsConstructor
public class ComponentLocation extends MessageComponent {
    private final double lat;
    private final double lon;
    private final String title;
    private final String content;

    @Override
    public String toString() {
        return "[CQ:location,lat=" + lat + ",lon=" + lon + ",title=" + title + ",content=" + content + "]";
    }
}
