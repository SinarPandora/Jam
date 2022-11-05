package cc.moecraft.icq.sender.message.components;

import cc.moecraft.icq.sender.message.MessageComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 语音转文字（腾讯 API）
 * 仅群聊可用
 * <p>
 * Author: sinar
 * 2021/3/25 21:54
 */
@Getter
@AllArgsConstructor
public class ComponentTTS extends MessageComponent {
    private final String message;

    @Override
    public String toString() {
        return String.format("[CQ:tts,text=%s]", message);
    }
}
