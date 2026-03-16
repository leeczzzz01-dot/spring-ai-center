package agent.ai.api.service.impl;

import agent.ai.api.advisor.UserInfoAdvisor;
import agent.ai.api.service.AiStrategyService;
import agent.ai.api.utils.ThreadContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;

@Service
public class AiStrategyServiceImpl implements AiStrategyService {

    @Autowired
    public Map<String, ChatClient> chatClientMap;

    /**
     * 执行链
     * 1. 获取对应的 AI 模型
     * 2. 获取动态提示词
     * 3. 聊天记忆模块
     * 4. 工具调用
     */

    /**
     * 策略调用方法
     * @param modelType 传入 "google" 或 "zhipu"
     * @param message 用户发的消息
     */
    public  Flux<String> chatStream(String modelType, String message) {
        // 1. 拼装 Bean 的名字
        String beanName = modelType + "ChatClient";
        // 2. 从 Map 中获取对应的 AI 客户端
        System.out.println("beanName: " + beanName);
        System.out.println("有如下模型: ");
        chatClientMap.forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });
        ChatClient aiClient = chatClientMap.get(beanName);
        if (aiClient == null) {
            throw new IllegalArgumentException("未找到对应的 AI 模型: " + modelType);
        }
        String userInfo;
        if (null != ThreadContext.get()){
            userInfo =  ThreadContext.get().toString();
        } else {
            userInfo = "";
        }
        // 3. 发起调用
        Flux<ChatResponse> chatResponseFlux = aiClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "12345")
                        .param(UserInfoAdvisor.USER_INFO_KEY, userInfo))
                .stream()
                .chatResponse();

        return chatResponseFlux.mapNotNull(response -> {
            // 2. 检查这段流里有没有携带 Token 消耗信息 (通常在最后一段才会有)
            if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                Integer totalTokens = response.getMetadata().getUsage().getTotalTokens();

                // 只有当 totalTokens 明确大于 0（说明流结束了，或者带了真实数据）时才打印
                if (totalTokens != null && totalTokens > 0) {
                    Integer promptTokens = response.getMetadata().getUsage().getPromptTokens();
                    Integer generationTokens = response.getMetadata().getUsage().getCompletionTokens();

                    System.out.println("======================================");
                    System.out.println("[" + modelType + "] 提问消耗 Token: " + promptTokens);
                    System.out.println("[" + modelType + "] 回答消耗 Token: " + generationTokens);
                    System.out.println("[" + modelType + "] 总计消耗 Token: " + totalTokens);
                    System.out.println("======================================");
                }
            }

            // 3. 把文字提取出来，单独返回给前端展示 (防空指针处理)

            return Optional.ofNullable(response.getResult())
                    .map(Generation::getOutput)
                    .map(AbstractMessage::getText)
                    .orElse(""); // 如果这一块没有文字，就返回空字符串
        });
    }

    public String chatCall(String modelName, String message) {
        String beanName = modelName + "ChatClient";
        ChatClient targetClient = chatClientMap.get(beanName);

        if (targetClient == null) {
            return "系统提示：未找到名为 [" + modelName + "] 的模型配置！";
        }

        // 1. 使用 call() 获取完整的 ChatResponse 对象
        ChatResponse response = targetClient.prompt()
                .user(message)
                .call()
                .chatResponse();
        // 3. 提取纯文本返回给浏览器
        if (response.getResult() != null && response.getResult().getOutput() != null) {
            return response.getResult().getOutput().getText();
        }

        return "未生成任何内容";
    }
}