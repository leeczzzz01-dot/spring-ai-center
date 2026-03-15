package agent.ai.api.config;

import agent.ai.api.repository.DBChatMemoryRepository;
import agent.ai.api.service.impl.AIChatMemoryByDBService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
    @Autowired
    private DBChatMemoryRepository chatMemoryRepository;
    // 1. 组装 Google 的客户端
    @Bean(name = "googleChatClient")
    public ChatClient googleChatClient(GoogleGenAiChatModel googleModel) {
        return capabilityConfig(googleModel);
    }

    // 2. 组装 智谱 的客户端
    @Bean(name = "zhipuChatClient")
    public ChatClient zhipuChatClient(ZhiPuAiChatModel zhipuModel) {
        return capabilityConfig(zhipuModel);
    }

    private ChatClient capabilityConfig(ChatModel chatModel){
        ChatMemory chatMemory = AIChatMemoryByDBService.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        return ChatClient.builder(chatModel).defaultAdvisors(
                        // 日志
                        new SimpleLoggerAdvisor(),
                        // 聊天记忆
                        PromptChatMemoryAdvisor.builder(chatMemory).systemPromptTemplate().order(-100).build()
                )
                .build();
    }

}