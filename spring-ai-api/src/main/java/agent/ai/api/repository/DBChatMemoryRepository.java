package agent.ai.api.repository;

import agent.ai.api.constant.ChatConstants;
import agent.ai.api.mapper.ChatMessageMapper;
import agent.ai.api.mapper.ChatSessionMapper;
import agent.ai.api.pojo.po.ChatMessage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lichen
 * @date 2026/3/14
 * @description:
 */
@Repository
public class DBChatMemoryRepository implements ChatMemoryRepository {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // 获取会话 ID 列表
    @Override
    public List<String> findConversationIds() {
        return chatSessionMapper.selectList(null).stream()
                .map(session -> String.valueOf(session.getId()))
                .collect(Collectors.toList());
    }

    // 根据会话 ID 获取消息列表
    @Override
    public List<Message> findByConversationId(String conversationId) {
        Long sessionId = Long.valueOf(conversationId);
        // 按创建时间升序排列，确保上下文顺序正确
        List<ChatMessage> dbMessages = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreatedAt));

        List<Message> messages = new ArrayList<>();
        for (ChatMessage dbMsg : dbMessages) {
            Integer role = dbMsg.getRole();
            String content = dbMsg.getContent();
            if (ChatConstants.ROLE_USER.equals(role)) {
                messages.add(new UserMessage(content));
            } else if (ChatConstants.ROLE_ASSISTANT.equals(role)) {
                messages.add(new AssistantMessage(content));
            } else if (ChatConstants.ROLE_SYSTEM.equals(role)) {
                messages.add(new SystemMessage(content));
            }
            // 暂未处理工具消息的还原逻辑，可按需补充
        }
        return messages;
    }

    // 保存消息列表
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(String conversationId, List<Message> messages) {
        Long sessionId = Long.valueOf(conversationId);

        // 1. 构建待保存的消息列表
        List<ChatMessage> dbMessages = new ArrayList<>();
        for (Message msg : messages) {
            ChatMessage dbMsg = new ChatMessage();
            dbMsg.setSessionId(sessionId);

            // 将 MessageType 转换为数字常量
            Integer role = ChatConstants.ROLE_USER;
            MessageType type = msg.getMessageType();
            if (MessageType.USER == type) {
                role = ChatConstants.ROLE_USER;
            } else if (MessageType.ASSISTANT == type) {
                role = ChatConstants.ROLE_ASSISTANT;
            } else if (MessageType.SYSTEM == type) {
                role = ChatConstants.ROLE_SYSTEM;
            } else if (MessageType.TOOL == type) {
                role = ChatConstants.ROLE_TOOL;
            }
            dbMsg.setRole(role);
            dbMsg.setContent(msg.getText());
            dbMsg.setCreatedAt(LocalDateTime.now());
            dbMsg.setSessionId(sessionId);
            // 存储 metadata 的 JSON 字符串 (确保格式符合数据库 JSON 类型要求)
            if (msg.getMetadata() != null && !msg.getMetadata().isEmpty()) {
                try {
                    dbMsg.setMetadata(objectMapper.writeValueAsString(msg.getMetadata()));
                } catch (JsonProcessingException e) {
                    dbMsg.setMetadata("{}"); // 序列化失败时存入空 JSON 对象
                }
            } else {
                dbMsg.setMetadata("{}"); // 默认为空 JSON 对象防止 DDL 校验失败
            }
            dbMessages.add(dbMsg);
        }

        // 2. 执行批量保存
        if (!dbMessages.isEmpty()) {
            chatMessageMapper.insertBatch(dbMessages);
        }
    }

    // 删除会话
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByConversationId(String conversationId) {
        Long sessionId = Long.valueOf(conversationId);
        chatMessageMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId));
    }
}
