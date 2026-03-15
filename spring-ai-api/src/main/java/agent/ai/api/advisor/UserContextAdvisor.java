package agent.ai.api.advisor;

import agent.ai.api.pojo.po.User;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import agent.ai.api.utils.ThreadContext;

import java.util.ArrayList;
import java.util.List;

public class UserContextAdvisor implements Advisor {


    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    // ... 省略 getOrder 等方法
}