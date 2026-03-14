package agent.ai.api.config;

import agent.ai.api.memory.DBChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Schedulers;

@Configuration
public class AiConfig {
    @Autowired
    private DBChatMemoryRepository chatMemory;
    // 1. 组装 Google 的客户端
    @Bean(name = "googleChatClient")
    public ChatClient googleChatClient(GoogleGenAiChatModel googleModel) {
        // googleModel 是 Spring 自动根据 application.yml 里的 key 创建好的，直接用
        return ChatClient.builder(googleModel)
                .defaultSystem("你是一个AI")
                .defaultAdvisors(
                        // 日志
                        new SimpleLoggerAdvisor(),
                        // 聊天记忆
                        MessageChatMemoryAdvisor.builder((ChatMemory) chatMemory).build()
                )
                .build();
    }

    // 2. 组装 智谱 的客户端
    @Bean(name = "zhipuChatClient")
    public ChatClient zhipuChatClient(ZhiPuAiChatModel zhipuModel) {
        return ChatClient.builder(zhipuModel).build();
    }
}