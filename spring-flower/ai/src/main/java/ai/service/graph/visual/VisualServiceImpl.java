package ai.service.graph.visual;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class VisualServiceImpl implements VisualService {

    @Resource(name = "visualChatClient")
    private ChatClient visualChatClient;

    @Override
    public Flux<String> chat(Media media) {
        return visualChatClient.prompt()
                .user(promptUserSpec -> promptUserSpec.text("你是一个花艺信息提取助手。根据图片信息, 提取信息: " +
                                "color（颜色，如：红色、粉色、白色)"+
                                "name (花的专业名或者小名或者编一个)")
                        .media(media))
                .stream()
                .content()
                .concatWith(Flux.just("stop"));
    }
}
