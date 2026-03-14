//package agent.ai.api.chain.handlers;
//
//import agent.ai.api.chain.config.AiHandler;
//import agent.ai.api.pojo.po.User;
//import agent.ai.api.utils.ThreadContext;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.prompt.PromptTemplate;
//import org.springframework.ai.template.st.StTemplateRenderer;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * @author lichen
// * @date 2026/3/14
// * @description:
// */
//public class PromptEnrichHandler implements AiHandler {
//    @Override
//    public ChatClient.ChatClientRequestSpec handler(ChatClient chatClient) {
//        PromptTemplate promptTemplate = PromptTemplate.builder()
//                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
//                .template("Tell me the names of 5 movies whose soundtrack was composed by <composer>.")
//                .build();
//        User user = ThreadContext.get();
//        String prompt = promptTemplate.render(Map.of("composer", "John Williams"));
//        chatClient.prompt().advisors(
//                VectorStoreChatMemoryAdvisor.builder(vectorStore)
//                        .promptTemplate(new PromptTemplate("历史背景：{long_term_memory} 指令：{instructions}"))
//                        .build(),
//        );
//    }
//}
