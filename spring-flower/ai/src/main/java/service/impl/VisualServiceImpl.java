package service.impl;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import service.VisualService;

@Service
public class VisualServiceImpl implements VisualService {

    @Resource(name = "visualChatClient")
    private ChatClient visualChatClient;

    @Override
    public Flux<String> chat(Media media) {
        return visualChatClient.prompt()
                .user(promptUserSpec -> promptUserSpec.text("识别有哪些食物,饮料？").media(media))
                .stream()
                .content();
    }

}
