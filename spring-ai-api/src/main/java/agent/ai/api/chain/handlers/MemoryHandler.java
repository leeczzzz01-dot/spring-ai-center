package agent.ai.api.chain.handlers;

import agent.ai.api.chain.config.AiHandler;
import org.springframework.ai.chat.client.ChatClient;

/**
 * @author lichen
 * @date 2026/3/14
 * @description:
 */
public class MemoryHandler implements AiHandler {

    @Override
    public ChatClient.ChatClientRequestSpec handler(ChatClient chatClient) {
        return null;
    }
}
