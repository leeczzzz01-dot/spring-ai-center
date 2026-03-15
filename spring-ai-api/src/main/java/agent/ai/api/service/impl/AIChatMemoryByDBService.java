package agent.ai.api.service.impl;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Service; // 必须添加
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 自定义数据库聊天记忆实现
 */
public class AIChatMemoryByDBService implements ChatMemory {

    private static final int DEFAULT_MAX_MESSAGES = 20;

    private final ChatMemoryRepository chatMemoryRepository;

    public AIChatMemoryByDBService(ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
    }


    @Override
    public void add(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        this.chatMemoryRepository.saveAll(conversationId, messages);
    }

    @Override
    public List<Message> get(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        List<Message> messages = this.chatMemoryRepository.findByConversationId(conversationId);
        return messages != null ? messages : new ArrayList<>();
    }

    @Override
    public void clear(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        this.chatMemoryRepository.deleteByConversationId(conversationId);
    }

    // 5. 修正 Builder：必须返回 AIChatMemoryByDB 而不是官方的类
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ChatMemoryRepository chatMemoryRepository;

        public Builder chatMemoryRepository(ChatMemoryRepository chatMemoryRepository) {
            this.chatMemoryRepository = chatMemoryRepository;
            return this;
        }


        public AIChatMemoryByDBService build() {
            return new AIChatMemoryByDBService(this.chatMemoryRepository);
        }
    }
}