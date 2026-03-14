package agent.admin.api.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * @description: 用户实体类，映射 sys_user 表
 */
@Data
@TableName("sys_user")
public class User {
    
    /**
     * 用户ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 登录用户名，唯一标识
     */
    private String username;
    
    /**
     * 登录密码 (不参与序列化存入 Token)
     */
    @JsonIgnore
    private String password;
    
    /**
     * 用户昵称或显示名称
     */
    private String nickname;
    
    /**
     * 性别（如 1:男，2:女，0:未知）
     */
    private Integer sex;
    
    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;
}
