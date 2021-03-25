package cc.moecraft.icq.sender.message.components;

import cc.moecraft.icq.sender.message.MessageComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 真·戳一戳
 * <p>
 * Author: sinar
 * 2021/3/25 21:54
 */
@Getter
@AllArgsConstructor
public class ComponentPoke extends MessageComponent {
    private final long qId;

    @Override
    public String toString() {
        return String.format("[CQ:poke,qq=%s]", qId);
    }
}
