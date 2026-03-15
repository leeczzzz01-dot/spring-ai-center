package agent.ai.api.mapper;

import agent.ai.api.pojo.po.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    int insertBatch(@Param("list") List<ChatMessage> list);
}