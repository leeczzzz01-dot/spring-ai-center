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
     * 角色标识（1:user, 2:assistant, 3:system, 4:tool）
     */
    private Integer role;

    /**
     * 消息的主要内容文本
     */
    private String content;

    /**
     * 提问消耗的 Token 数 (Input)
     */
    private Integer promptTokens;

    /**
     * 回答生成的 Token 数 (Output)
     */
    private Integer completionTokens;

    /**
     * 总计消耗的 Token 数
     */
    private Integer totalTokens;

    /**
     * 扩展元数据（JSON 格式）
     */
    private String metadata;

    /**
     * 消息产生的时间
     */
    private LocalDateTime createdAt;
}