package agent.ai.api.chain.config;

import org.springframework.ai.chat.client.ChatClient;

/**
 * @author lichen
 * @date 2026/3/14
 * @description:
 */
public interface AiHandler {
    ChatClient.ChatClientRequestSpec handler(ChatClient chatClient );
}
