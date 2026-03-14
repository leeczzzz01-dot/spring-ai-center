package agent.ai.api.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * @description: 聊天消息明细实体类，映射 chat_message 表
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    /**
     * 消息ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的会话ID (chat_session.id)
     */
    private Long sessionId;

    /**
     * 角色标识（如 'user', 'assistant', 'system' 等）
     */
    private String role;

    /**
     * 消息的主要内容文本
     */
    private String content;

    /**
     * 此次消息产生的 Token 消耗量
     */
    private Integer tokenUsage;

    /**
     * 扩展元数据（例如模型耗时、附加参数等，存为JSON字符串，可用TypeHandler映射对象）
     */
    private String metadata;

    /**
     * 消息产生的时间
     */
    private LocalDateTime createdAt;
}