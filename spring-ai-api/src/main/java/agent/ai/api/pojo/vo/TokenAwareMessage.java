package agent.ai.api.pojo.vo;

import lombok.Data;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.util.Map;

/**
 * 带有 Token 消耗感知能力的 Message 包装类
 */
@Data
public class TokenAwareMessage implements Message {

    private final Message originalMessage;
    private final Integer promptTokens;
    private final Integer completionTokens;
    private final Integer totalTokens;

    public TokenAwareMessage(Message originalMessage, Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        this.originalMessage = originalMessage;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }


    // 实现 Message 接口的所有方法，委托给原始消息
    @Override public String getText() { return originalMessage.getText(); }
    @Override public MessageType getMessageType() { return originalMessage.getMessageType(); }
    @Override public Map<String, Object> getMetadata() { return originalMessage.getMetadata(); }
}