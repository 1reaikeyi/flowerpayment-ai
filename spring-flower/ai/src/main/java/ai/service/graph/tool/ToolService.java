package ai.service.graph.tool;

import reactor.core.publisher.Flux;

public interface ToolService {
    Flux<String> chat(String visualValue, String question);
}
