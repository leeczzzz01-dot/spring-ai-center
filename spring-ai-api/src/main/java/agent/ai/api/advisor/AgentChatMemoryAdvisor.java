package agent.ai.api.advisor;

import agent.ai.api.pojo.vo.TokenAwareMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


// 完成记忆封装
@Slf4j
public final class AgentChatMemoryAdvisor implements BaseChatMemoryAdvisor {
	private static final String CONTEXT_USER_QUESTION_KEY = "CONTEXT_USER_QUESTION_KEY";

	private static final PromptTemplate DEFAULT_SYSTEM_PROMPT_TEMPLATE = new PromptTemplate("""
			{instructions}

			使用memory部分中的对话记忆用来丰富你和用户对话的上下文。

			---------------------
			memory:
			{memory}
			---------------------
			
			你根据userInfo对用户进行一个基础认定
			userInfo:
			----------------
			<userInfo>
			----------------
			""");

	private final PromptTemplate systemPromptTemplate;

	private final String defaultConversationId;

	private final int order;

	private final Scheduler scheduler;

	private final ChatMemory chatMemory;

	private AgentChatMemoryAdvisor(ChatMemory chatMemory, String defaultConversationId, int order, Scheduler scheduler,
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
		// 1. Retrieve the chat memory for the current conversation.
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
		// 4. 将数据拼接搭配 prompt 中
		ChatClientRequest processedChatClientRequest = chatClientRequest.mutate()
				.prompt(chatClientRequest.prompt().augmentSystemMessage(augmentedSystemText))
				.build();

		// 5. 将数据存放到等待 token 数据获取到后进行统一操作
		UserMessage userMessage = processedChatClientRequest.prompt().getUserMessage();

		chatClientRequest.context().put(CONTEXT_USER_QUESTION_KEY, userMessage.getText());
		return processedChatClientRequest;
	}

	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
		// 1. 获取用户问题与响应对象
		String userQuestion = (String) chatClientResponse.context().getOrDefault(CONTEXT_USER_QUESTION_KEY, "");
		ChatResponse chatResponse = chatClientResponse.chatResponse();

		// 2. 预准备待存消息容器
		List<Message> insertMessages = new ArrayList<>();

		// 3. 核心逻辑：当且仅当存在有效回复时处理
		if (chatResponse != null && !chatResponse.getResults().isEmpty()) {
			// A. 提取 Usage
			Usage usage = chatResponse.getMetadata().getUsage();

			if (usage != null) {
				Integer prompt = usage.getPromptTokens();
				Integer completion = usage.getCompletionTokens();
				Integer total = usage.getTotalTokens();

				// B. 构建 User 消息并封装 Token
				insertMessages.add(new TokenAwareMessage(new UserMessage(userQuestion), prompt, completion, total));

				// C. 批量转化并封装 Assistant 消息
				chatResponse.getResults().stream()
						.map(generation -> new TokenAwareMessage(generation.getOutput(), prompt, completion, total))
						.forEach(insertMessages::add);

				// D. 批量打印日志
				log.info("会话: {} | 提示词 Token: {} | 回答 Token: {} | 总消耗: {}",
						defaultConversationId, prompt, completion, total);
			}
		} else {
			log.warn("用户问：{}，AI 未返回任何回答", userQuestion);
		}

		// 4. 执行持久化
		if (!insertMessages.isEmpty()) {
			this.chatMemory.add(this.defaultConversationId, insertMessages);
		}

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
		public AgentChatMemoryAdvisor build() {
			return new AgentChatMemoryAdvisor(this.chatMemory, this.conversationId, this.order, this.scheduler, this.systemPromptTemplate);
		}
	}
}