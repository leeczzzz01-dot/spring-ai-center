package agent.ai.api.memory;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * @author lichen
 * @date 2026/3/14
 * @description:
 */
public class DBChatMemoryRepository implements ChatMemoryRepository {

    // 获取会话 ID 列表
    @Override
    public List<String> findConversationIds() {
        return null;
    }

    // 根据会话 ID 获取消息列表
    @Override
    public List<Message> findByConversationId(String conversationId) {
        return null;
    }

    // 保存消息列表
    @Override
    public void saveAll(String conversationId, List<Message> messages) {

    }

    // 删除会话
    @Override
    public void deleteByConversationId(String conversationId) {

    }
}
