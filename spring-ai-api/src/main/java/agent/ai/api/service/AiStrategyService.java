package agent.ai.api.service;

import reactor.core.publisher.Flux;

/**
 * @author lichen
 * @date 2026/3/12
 * @description:
 */
public interface AiStrategyService {

    public Flux<String> chatStream(String modelType, String message);

    public String chatCall(String modelName, String message);
}
