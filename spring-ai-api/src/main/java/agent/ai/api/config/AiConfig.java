package agent.ai.api.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    // 1. 组装 Google 的客户端
    @Bean(name = "googleChatClient")
    public ChatClient googleChatClient(GoogleGenAiChatModel googleModel) {
        // googleModel 是 Spring 自动根据 application.yml 里的 key 创建好的，直接用
        return ChatClient.builder(googleModel).defaultSystem().build();
    }

    // 2. 组装 智谱 的客户端
    @Bean(name = "zhipuChatClient")
    public ChatClient zhipuChatClient(ZhiPuAiChatModel zhipuModel) {
        return ChatClient.builder(zhipuModel).build();
    }
}