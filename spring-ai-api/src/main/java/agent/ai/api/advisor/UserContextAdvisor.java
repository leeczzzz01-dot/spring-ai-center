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
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        User user = ThreadContext.get();
        if (user == null) {
            return request; // 没登录，直接放行
        }

        // 1. 从 request 中扒出底层的 Prompt，并拷贝一份它的消息列表
        Prompt originalPrompt = request.prompt();
        List<Message> messages = new ArrayList<>(originalPrompt.getInstructions());

        boolean foundSystem = false;

        // 2. 遍历消息列表，寻找 SystemMessage
        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            if (msg instanceof SystemMessage sysMsg) {
                // 找到了！获取原来的文本，加上我们的用户信息
                String newText = sysMsg.getContent() +
                        "\n【当前用户】" + user.getNickname();
                // 替换掉原来的 SystemMessage
                messages.set(i, new SystemMessage(newText));
                foundSystem = true;
                break; // 通常只有一个 SystemMessage，找到就撤
            }
        }

        // 3. 如果之前的代码里压根没配 SystemMessage，我们就自己建一个放在最前面
        if (!foundSystem) {
            messages.add(0, new SystemMessage("当前对话用户是：" + user.getNickname()));
        }

        // 4. 用修改后的消息列表，重新建一个 Prompt 对象 (保留原来的 options)
        Prompt newPrompt = new Prompt(messages, originalPrompt.getOptions());

        // 5. 重点来了！调用你源码里贴的 mutate() 方法，把新 Prompt 塞进去！
        return request.mutate()
                .prompt(newPrompt)
                .build();
    }

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