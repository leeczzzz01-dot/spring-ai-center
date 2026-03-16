package agent.ai.api.advisor;

import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.util.Assert;
import reactor.core.scheduler.Scheduler;

import java.util.Map;


public class UserInfoAdvisor implements BaseAdvisor {
    public static final String USER_INFO_KEY = "userInfo";
    private static final PromptTemplate DEFAULT_SYSTEM_USER_PROMPT_TEMPLATE = new PromptTemplate("""
            用户基本信息:
            ----------------
            <userInfo>
            ----------------
            """);

    private final PromptTemplate systemUserPromptTemplate;

    private final int order;

    private final Scheduler scheduler;


    private UserInfoAdvisor(int order, Scheduler scheduler, PromptTemplate systemUserPromptTemplate) {
        Assert.notNull(scheduler, "scheduler cannot be null");
        Assert.notNull(systemUserPromptTemplate, "systemPromptTemplate cannot be null");
        this.order = order;
        this.scheduler = scheduler;
        this.systemUserPromptTemplate = systemUserPromptTemplate;
    }
    @Override
    public String getName() {
        return "UserInfoAdvisor";
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        Object userInfoObj = chatClientRequest.context().get(USER_INFO_KEY);
        String userInfoStr = (userInfoObj != null) ? userInfoObj.toString() : "";
        String augmentedSystemText = this.systemUserPromptTemplate.render(Map.of("userInfo", userInfoStr));
        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentSystemMessage(
                        systemMessage -> systemMessage.mutate().text(systemMessage.getText() + augmentedSystemText).build()
                ))
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }


    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for PromptChatMemoryAdvisor.
     */
    public static final class Builder {

        private PromptTemplate systemUserPromptTemplate = DEFAULT_SYSTEM_USER_PROMPT_TEMPLATE;

        private int order = Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER;

        private Scheduler scheduler = BaseAdvisor.DEFAULT_SCHEDULER;


        private Builder() {
        }

        /**
         * Set the system prompt template.
         *
         * @param systemUserPromptTemplate the system prompt template
         * @return the builder
         */

        public Builder systemUserPromptTemplate(PromptTemplate systemUserPromptTemplate) {
            this.systemUserPromptTemplate = systemUserPromptTemplate;
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
        public UserInfoAdvisor build() {
            return new UserInfoAdvisor(this.order, this.scheduler, this.systemUserPromptTemplate);
        }
    }
}