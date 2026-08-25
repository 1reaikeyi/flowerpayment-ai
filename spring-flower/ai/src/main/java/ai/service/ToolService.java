package ai.service;

import reactor.core.publisher.Flux;

public interface ToolService {
    Flux<String> chat(String message);
}
