package agent.ai.api.memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author lichen
 * @date 2026/3/14
 * @description:
 */
public class ChatMemory {
    public void doIt(ChatClient chatClient){
        String systemTemplate = "你是一个心理专家，当前正在和 <userName> 交流。";
        Message systemMessage = new SystemMessage(
                new PromptTemplate(systemTemplate).render(Map.of("userName", "小明"))
        );


        // 2. 用户的具体问题
        Message userMessage = new UserMessage("我感觉压力很大。");
        // 3. 构建 Prompt
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        chatClient.prompt(prompt);
        chatClient.prompt().system();
    }

}
