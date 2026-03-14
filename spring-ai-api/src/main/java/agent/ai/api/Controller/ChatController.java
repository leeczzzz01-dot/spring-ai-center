package agent.ai.api.Controller;

import agent.ai.api.service.AiStrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private AiStrategyService aiStrategyService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<String> streamChatTest(String message,String modelName) {
        return aiStrategyService.chatStream(modelName, message);
    }

    @GetMapping(value = "/call")
    public String callChat(String message,String modelName) {
        return aiStrategyService.chatCall(modelName, message);
    }

}