package agent.ai.api.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.*;
import java.util.stream.Collectors;


// 完成记忆封装
@Slf4j
public final class AgentSystemPromptAdvisor implements BaseChatMemoryAdvisor {
    private static final String CONTEXT_USER_QUESTION_KEY = "CONTEXT_USER_QUESTION_KEY";

    private static final PromptTemplate DEFAULT_SYSTEM_PROMPT_TEMPLATE = new PromptTemplate("""
			{instructions}

			使用“记忆”部分中的对话记忆来提供准确的答案。

			---------------------
			记忆:
			{memory}
			---------------------
			
			用户基本信息:
			----------------
			<userInfo>
			----------------
			""");

    private final PromptTemplate systemPromptTemplate;

    private final String defaultConversationId;

    private final int order;

    private final Scheduler scheduler;

    private final ChatMemory chatMemory;

    private AgentSystemPromptAdvisor(ChatMemory chatMemory, String defaultConversationId, int order, Scheduler scheduler,
                                   PromptTemplate systemPromptTemplate) {
        Assert.notNull(chatMemory, "chatMemory cannot be null");
        Assert.hasText(defaultConversationId, "defaultConversationId cannot be null or empty");
        Assert.notNull(scheduler, "scheduler cannot be null");
        Assert.notNull(systemPromptTemplate, "systemPromptTemplate cannot be null");
        this.chatMemory = chatMemory;
        this.defaultConversationId = defaultConversationId;
        this.order = order;
        this.scheduler = scheduler;
        this.systemPromptTemplate = systemPromptTemplate;
    }

    public static Builder builder(ChatMemory chatMemory) {
        return new Builder(chatMemory);
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String conversationId = getConversationId(chatClientRequest.context(), this.defaultConversationId);
        // 1. 获取记忆
        List<Message> memoryMessages = this.chatMemory.get(conversationId);
        log.debug("[PromptChatMemoryAdvisor.before]  conversationId={}: 记忆数据: {}", conversationId, memoryMessages);

        // 2. Message转换
        String memory = memoryMessages.stream()
                .filter(m -> m.getMessageType() == MessageType.USER || m.getMessageType() == MessageType.ASSISTANT)
                .map(m -> m.getMessageType() + ":" + m.getText())
                .collect(Collectors.joining(System.lineSeparator()));

        // 3. 装填 SystemMessage DefaultChatClientUtils.toChatClientRequest 将systemTest 这些转换成了SystemMessage
        // 此处不可省略
        SystemMessage systemMessage = chatClientRequest.prompt().getSystemMessage();
        String augmentedSystemText = this.systemPromptTemplate
                .render(Map.of("instructions", systemMessage.getText(), "memory", memory));

        // 4. 填充问题
        String question = this.systemPromptTemplate
                .render(Map.of("instructions", systemMessage.getText(), "memory", memory));
        // 4. 将数据拼接搭配 prompt 中
        ChatClientRequest processedChatClientRequest = chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentSystemMessage(augmentedSystemText))
                .build();

        // 5. 将数据存放到等待 token 数据获取到后进行统一操作
        UserMessage userMessage = processedChatClientRequest.prompt().getUserMessage();

        chatClientRequest.context().put(CONTEXT_USER_QUESTION_KEY, userMessage.getText());
        return processedChatClientRequest;
    }

    // 不做处理
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }


    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                                 StreamAdvisorChain streamAdvisorChain) {
        // Get the scheduler from BaseAdvisor
        Scheduler scheduler = this.getScheduler();

        // Process the request with the before method
        return Mono.just(chatClientRequest)
                .publishOn(scheduler)
                .map(request -> this.before(request, streamAdvisorChain))
                .flatMapMany(streamAdvisorChain::nextStream)
                .transform(flux -> new ChatClientMessageAggregator().aggregateChatClientResponse(flux,
                        response -> this.after(response, streamAdvisorChain)));
    }

    /**
     * Builder for PromptChatMemoryAdvisor.
     */
    public static final class Builder {

        private PromptTemplate systemPromptTemplate = DEFAULT_SYSTEM_PROMPT_TEMPLATE;

        private String conversationId = ChatMemory.DEFAULT_CONVERSATION_ID;

        private int order = Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER;

        private Scheduler scheduler = BaseAdvisor.DEFAULT_SCHEDULER;

        private ChatMemory chatMemory;

        private Builder(ChatMemory chatMemory) {
            this.chatMemory = chatMemory;
        }

        /**
         * Set the system prompt template.
         *
         * @param systemPromptTemplate the system prompt template
         * @return the builder
         */
        public Builder systemPromptTemplate(PromptTemplate systemPromptTemplate) {
            this.systemPromptTemplate = systemPromptTemplate;
            return this;
        }

        /**
         * Set the conversation id.
         *
         * @param conversationId the conversation id
         * @return the builder
         */
        public Builder conversationId(String conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public Builder scheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        /**
         * Set the order.
         *
         * @param order the order
         * @return the builder
         */
        public Builder order(int order) {
            this.order = order;
            return this;
        }

        /**
         * Build the advisor.
         *
         * @return the advisor
         */
        public AgentSystemPromptAdvisor build() {
            return new AgentSystemPromptAdvisor(this.chatMemory, this.conversationId, this.order, this.scheduler, this.systemPromptTemplate);
        }
    }
}