package agent.ai.api.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * @description: 聊天会话实体类，映射 chat_session 表
 */
@Data
@TableName("chat_session")
public class ChatSession {

    /**
     * 会话ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID，关联 sys_user
     */
    private Long userId;

    /**
     * 会话主题或标题
     */
    private String topic;

    /**
     * 会话状态 (0: 进行中, 1: 已结束)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}