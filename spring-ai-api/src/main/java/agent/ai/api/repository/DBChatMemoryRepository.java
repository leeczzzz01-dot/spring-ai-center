package agent.ai.api.repository;

import agent.ai.api.constant.ChatConstants;
import agent.ai.api.mapper.ChatMessageMapper;
import agent.ai.api.mapper.ChatSessionMapper;
import agent.ai.api.pojo.po.ChatMessage;
import agent.ai.api.pojo.vo.TokenAwareMessage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lichen
 * @date 2026/3/14
 * @description:
 */
@Repository
@Slf4j
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
    public void saveAll(String conversationId, List<Message> messages) {
        // 1. 安全转换会话 ID（增加判空和异常处理，防止之前的 NumberFormatException）
        Long sessionId = Long.valueOf(conversationId);
        LocalDateTime now = LocalDateTime.now();

        // 2. 利用 Stream 进行流式转换
        List<ChatMessage> dbMessages = messages.stream()
                .map(msg -> convertToEntity(sessionId, msg, now))
                .toList();

        // 3. 执行批量保存
        if (!dbMessages.isEmpty()) {
            chatMessageMapper.insertBatch(dbMessages);
        }
    }

    private ChatMessage convertToEntity(Long sessionId, Message msg, LocalDateTime now) {
        ChatMessage dbMsg = new ChatMessage();
        dbMsg.setSessionId(sessionId);
        dbMsg.setCreatedAt(now);
        dbMsg.setContent(msg.getText());
        // 处理 Role 映射
        dbMsg.setRole(switch (msg.getMessageType()) {
            case USER -> ChatConstants.ROLE_USER;
            case ASSISTANT -> ChatConstants.ROLE_ASSISTANT;
            case SYSTEM -> ChatConstants.ROLE_SYSTEM;
            case TOOL -> ChatConstants.ROLE_TOOL;
            default -> ChatConstants.ROLE_USER;
        });
        // 处理 Token 信息 (安全提取)
        if (msg instanceof TokenAwareMessage tam) {
            dbMsg.setTotalTokens(tam.getTotalTokens());
            dbMsg.setPromptTokens(tam.getPromptTokens());
            dbMsg.setCompletionTokens(tam.getCompletionTokens());
        }
        // 处理 Metadata JSON
        dbMsg.setMetadata(serializeMetadata(msg.getMetadata()));
        return dbMsg;
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.warn("Metadata 序列化失败，存入空对象", e);
            return "{}";
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
