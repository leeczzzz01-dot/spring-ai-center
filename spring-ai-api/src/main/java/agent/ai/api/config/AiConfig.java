package agent.ai.api.config;

import agent.ai.api.advisor.AgentChatMemoryAdvisor;
import agent.ai.api.repository.DBChatMemoryRepository;
import agent.ai.api.service.impl.AIChatMemoryByDBService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
    private static final String DEFAULT_SYSTEM_PROMPT_TEXT = """
        你是一个助手，请回答用户问题。
    """;

    private static final String DEFAULT_SYSTEM_TEXT_PROMPT_TEXT = """
        <instructions>
        """;
    private static final String SYSTEM_MEMORY_PROMPT_TEMPLATE_TEXT = """
            使用“记忆”部分中的对话记忆来提供准确的答案。
            ---------------------
            记忆:
            <memory>
            ---------------------
            """;

    private static final String SYSTEM_USERINFO_PROMPT_TEMPLATE_TEXT = """
            用户基本信息:
            ----------------
            <userInfo>
            ----------------
            """;

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
        return ChatClient.builder(chatModel)
                .defaultSystem(DEFAULT_SYSTEM_PROMPT_TEXT)
                .defaultAdvisors(
                        // 日志
                        new SimpleLoggerAdvisor(),
                        // 系统提示词
                        // 聊天记忆
                        AgentChatMemoryAdvisor.builder(chatMemory).systemPromptTemplate(getSystemMemoryPromptTemplate()).order(-100).build()
                        // 用户信息
                 //       AgentChatMemoryAdvisor.builder(chatMemory).systemPromptTemplate(getSystemUserInfoPromptTemplate()).order(-100).build()
                )
                .build();
    }

    private PromptTemplate getSystemMemoryPromptTemplate(){
        return PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template(SYSTEM_MEMORY_PROMPT_TEMPLATE_TEXT)
                .build();
    }

    private PromptTemplate getSystemUserInfoPromptTemplate(){
        return PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template(SYSTEM_USERINFO_PROMPT_TEMPLATE_TEXT)
                .build();
    }
}