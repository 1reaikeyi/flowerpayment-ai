package service.impl;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import service.ToolService;

@Service
public class ToolServiceImpl implements ToolService {
    @Resource(name = "toolClient")
    private ChatClient chatClient;
    @Override
    public Flux<String> chat(String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}
